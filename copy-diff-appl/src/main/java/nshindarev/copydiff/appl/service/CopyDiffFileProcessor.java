package nshindarev.copydiff.appl.service;

import nshindarev.copydiff.appl.config.Parameters;
import nshindarev.copydiff.appl.filter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by nshindarev on 20.08.16.
 */
public class CopyDiffFileProcessor implements Checker {

    private static final Logger logger = LoggerFactory.getLogger(CopyDiffFileProcessor.class);

    private Parameters   parameters;
    private Boolean      approved;
    private List<Filter> filters;

    public CopyDiffFileProcessor(Parameters parameters, List<Filter> filters) {
        this.parameters = parameters;
        this.filters = filters;
        this.approved = null;
    }

    @Override
    public void approve(Filter filter) {
        this.approved = true;
    }

    @Override
    public void reject(Filter filter) {
        this.approved = false;
    }

    public void process() {
        List<ContinueFilter> continueFilters = processFileFilters();

        checkBuffers(continueFilters);

        checkCompletes(continueFilters);

        copyFile();
    }

    private List<ContinueFilter> processFileFilters() {
        List<ContinueFilter> continueFilters = new ArrayList<>();
        // Для начала из всех фильтров применяем те, что реализуют интерфейс FileFilter
        for (Filter filter:filters) {
            if (filter instanceof FileFilter) {
                if (approved == null || approved) {
                    @SuppressWarnings("unchecked")
                    FileFilter fileFilter = (FileFilter) filter;
                    ContinueFilter continueFilter = fileFilter.check(parameters, this);
                    if (continueFilter != null) {
                        continueFilters.add(continueFilter);
                    }
                }
            } else if (filter instanceof ContinueFilter) {
                @SuppressWarnings("unchecked")
                ContinueFilter continueFilter = (ContinueFilter) filter;
                if (!continueFilters.contains(continueFilter)) {
                    continueFilters.add(continueFilter);
                }
            }
        }
        return continueFilters;
    }

    private void checkBuffers(List<ContinueFilter> continueFilters) {
        try {
            FileChannel fileChannel = FileChannel.open(parameters.getSourcePath(), StandardOpenOption.READ);
            // Смещение от начала файла
            long fileOffset = 0L;
            // Смещение от начала буфера минимальной обработанной порции данных
            int  bufferIncomplete = 0;
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*1024*1024);
            // Сколько в каждом буфере необработанных байт
            Map<BufferFileFilter, Integer> bufferIncompletes = new HashMap<>();
            // Данные из файла зачитываются от position(), которая выставлена после перенесённого буфера с предыдущего шага
            for (int readed=fileChannel.read(byteBuffer, fileOffset); readed > 0; readed=fileChannel.read(byteBuffer, fileOffset)) {
                // Ставим границу с учётом того, до куда причитано. Далее обрабатываем только до limit
                byteBuffer.limit(byteBuffer.position());
                for (ContinueFilter continueFilter : continueFilters.toArray(new ContinueFilter[] {})) {
                    if (continueFilter instanceof BufferFileFilter) {
                        @SuppressWarnings("unchecked")
                        BufferFileFilter bufferFileFilter = (BufferFileFilter)continueFilter;
                        int currentBufferIncomplete = bufferIncompletes.containsKey(bufferFileFilter) ? bufferIncompletes.get(bufferFileFilter) : 0;
                        int bufferPosition = bufferIncomplete - currentBufferIncomplete;
                        byteBuffer.position(bufferPosition);
                        // Берём буфер, который имитирует из нашего буфера только рабочую часть - position() в 0, а limit() в capacity()
                        ByteBuffer workedBuffer = byteBuffer.slice();
                        Integer completed = bufferFileFilter.bufferCheck(workedBuffer, fileOffset + bufferPosition, parameters, this);
                        if (completed == null) {
                            continueFilters.remove(bufferFileFilter);
                            completed = byteBuffer.limit();
                        } else {
                            completed += bufferPosition;
                        }
                        currentBufferIncomplete = byteBuffer.limit() - completed;
                        bufferIncompletes.put(bufferFileFilter, currentBufferIncomplete);
                        bufferIncomplete = Math.max(bufferIncomplete, currentBufferIncomplete);
                    }
                }
                if (bufferIncomplete > 0) {
                    byteBuffer.position(byteBuffer.limit() - bufferIncomplete);
                    ByteBuffer slice = byteBuffer.slice();
                    byteBuffer.position(0);
                    byteBuffer.put(slice);
                    byteBuffer.limit(byteBuffer.capacity());
                } else {
                    byteBuffer.clear();
                }
                fileOffset = fileOffset + readed;
            }
        } catch (IOException ioex) {
            logger.error("Reject: Произошла ошибка при открытии файла '{}'", parameters.getSourcePath().toFile().getAbsolutePath());
            reject(null);
            return;
        }
    }

    private void checkCompletes(List<ContinueFilter> continueFilters) {
        // Проверяем CompleteFilters
        for (ContinueFilter continueFilter:continueFilters) {
            if (continueFilter instanceof CompleteFilter) {
                @SuppressWarnings("unchecked")
                CompleteFilter completeFilter = (CompleteFilter)continueFilter;
                completeFilter.completeCheck(parameters, this);
            }
        }
    }

    private void copyFile() {
        // Проверяем результат проверок и принимаем решение о копировании файла
        if (approved == null) {
            logger.warn("Невозможно принять решение по файлу ''. Копирование не производится", parameters.getSourcePath().toFile().getAbsolutePath());
        } else if (!approved) {
            logger.debug("Копирования файла '{}' не производилось.", parameters.getSourcePath().toFile().getAbsolutePath());
        } else {
            try {
                File destFileDir = parameters.getDestPath().toFile().getParentFile();
                if (!destFileDir.exists()) {
                    if (destFileDir.mkdirs()) {
                        logger.debug("Папка '{}' успешно создана", destFileDir.getAbsolutePath());
                    } else {
                        logger.error("Не удалось создать папку при попытке скопировать файл: {}'", parameters.getDestPath());
                    }
                }
                if (destFileDir.exists()) {
                    Files.copy(parameters.getSourcePath(), parameters.getDestPath(), StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("Файл '{}' успешно скопирован.", parameters.getSourcePath().toFile().getAbsolutePath());
                }
            } catch (IOException e) {
                logger.error("Не удалось скопировать файл '{}' как '{}'", parameters.getSourcePath().toFile().getAbsolutePath(), parameters.getDestPath().toFile().getAbsolutePath());
            }
        }
    }

}
