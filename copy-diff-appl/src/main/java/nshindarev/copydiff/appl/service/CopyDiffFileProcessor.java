package nshindarev.copydiff.appl.service;

import nshindarev.copydiff.appl.config.Parameters;
import nshindarev.copydiff.appl.filter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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
        // Здесь будет код по обработке содержимого файла
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
