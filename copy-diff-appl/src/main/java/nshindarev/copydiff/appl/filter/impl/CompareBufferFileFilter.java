package nshindarev.copydiff.appl.filter.impl;

import nshindarev.copydiff.appl.config.Parameters;
import nshindarev.copydiff.appl.filter.BufferFileFilter;
import nshindarev.copydiff.appl.filter.Checker;
import nshindarev.copydiff.appl.filter.CompleteFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * Created by nshindarev on 21.08.16.
 */
public class CompareBufferFileFilter implements BufferFileFilter, CompleteFilter {

    private static final Logger logger = LoggerFactory.getLogger(CompareBufferFileFilter.class);

    FileChannel targetFileChannel;
    ByteBuffer  targetBuffer = null;
    boolean     equals = true;

    public CompareBufferFileFilter(Parameters parameters) throws IOException {
        targetFileChannel = FileChannel.open(parameters.getTargetPath(), StandardOpenOption.READ);
    }

    @Override
    public Integer bufferCheck(ByteBuffer byteBuffer, long filePosition, Parameters parameters, Checker checker) {
        assert byteBuffer != null;
        // Если буфер ещё не выделялся, то выделяем его размером с переданный в запросе
        // Не предполагается, что для сравнения будут приходить буфера разного размера
        if (targetBuffer == null) {
            targetBuffer = ByteBuffer.allocateDirect(byteBuffer.capacity());
        }
        targetBuffer.limit(byteBuffer.limit()-byteBuffer.position());
        targetBuffer.position(0);
        try {
            targetFileChannel.read(targetBuffer, filePosition);
        } catch (IOException ioe) {
            logger.error("Ошибка чтения из файла для сравнения '{}'", parameters.getTargetPath().toFile().getAbsolutePath(), ioe);
            checker.reject(this);
            close(parameters);
            return null;
        }
        // Если нашли различие в файле
        if (targetBuffer.compareTo(byteBuffer) != 0) {
            equals = false;
            logger.debug("Найдены различия файла '{}' при сравнении с содержимым шаблона", parameters.getSourcePath().toFile().getAbsolutePath());
            checker.approve(this);
            close(parameters);
            return null;
        }
        return targetBuffer.limit();
    }

    @Override
    public void completeCheck(Parameters parameters, Checker checker) {
        // Если не найдены различия в файлах
        if (equals) {
            logger.debug("Содержимое файла '{}' совпадает с содержимым шаблона", parameters.getSourcePath().toFile().getAbsolutePath());
            checker.reject(this);
        }
        close(parameters);
    }

    private void close(Parameters parameters) {
        if (targetFileChannel != null && targetFileChannel.isOpen()) {
            try {
                targetFileChannel.close();
            } catch (IOException ioe) {
                logger.trace("Призошла ошибка при попытке закрыть канал файла '{}'", parameters.getSourcePath().toFile().getAbsolutePath());
            }
        }
        targetFileChannel = null;
        targetBuffer = null;
    }

}
