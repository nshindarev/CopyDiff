package nshindarev.copydiff.appl.filter.impl;

import javafx.util.Pair;
import nshindarev.copydiff.appl.config.Parameters;
import nshindarev.copydiff.appl.filter.BufferFileFilter;
import nshindarev.copydiff.appl.filter.Checker;
import nshindarev.copydiff.search.TurboBoyerMoore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by nshindarev on 21.08.16.
 */
public class VirusBufferFileFilter implements BufferFileFilter {

    private static final Logger logger = LoggerFactory.getLogger(VirusBufferFileFilter.class);

    TurboBoyerMoore tbm;

    public VirusBufferFileFilter(byte[] virus) {
        this.tbm = TurboBoyerMoore.compile(virus);
    }

    // Создаёт фильтр, загружая вирус из потока
    public VirusBufferFileFilter(InputStream is) throws IOException {
        this(loadVirusFromStream(is));
    }

    // Создаёт фильтр, загружая вирус из файла, находящегося в пути
    public VirusBufferFileFilter(String resourceFileName) throws IOException {
        this(loadVirusFromStream(VirusBufferFileFilter.class.getClassLoader().getResourceAsStream(resourceFileName)));
    }

    @Override
    public Integer bufferCheck(ByteBuffer byteBuffer, long filePosition, Parameters parameters, Checker checker) {
        assert byteBuffer != null;
        Pair<Integer, List<Integer>> result = tbm.findOne(byteBuffer);
        if (result.getValue().size() > 0) {
            logger.info("Обнаружен вирус в файле '{}' [#{}]", parameters.getSourcePath().toFile().getAbsolutePath(), filePosition + result.getValue().get(0));
            checker.reject(this);
            return null;
        }
        return result.getKey();
    }

    private static final byte[] loadVirusFromStream(InputStream is) throws IOException {
        assert is != null;
        byte[] buff = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int len = is.read(buff); len > 0; len = is.read(buff)) {
            baos.write(buff, 0, len);
        }
        return baos.toByteArray();
    }

}
