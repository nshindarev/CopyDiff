package nshindarev.copydiff.appl.service;

import nshindarev.copydiff.appl.config.Parameters;
import nshindarev.copydiff.appl.filter.Filter;
import nshindarev.copydiff.appl.filter.impl.DiffFileFilter;
import nshindarev.copydiff.appl.filter.impl.ExeFileFilter;
import nshindarev.copydiff.appl.filter.impl.HiddenFileFilter;
import nshindarev.copydiff.appl.filter.impl.VirusBufferFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nshindarev on 17.08.16.
 */
public class CopyDiff {

    private static final Logger logger = LoggerFactory.getLogger(CopyDiff.class);

    private Parameters   parameters;
    private List<Filter> filters = new ArrayList<>();

    /**
     * Конструктор скрыт. Доступ производится через static метод обработки
     * @param parameters
     */
    private CopyDiff(Parameters parameters) throws IOException {
        // Предполагается, что без заполненных параметров сюда соваться не надо
        assert parameters != null && parameters.isComplete();
        this.parameters = parameters;
        filters.add(new DiffFileFilter());
        filters.add(new HiddenFileFilter());
        filters.add(new ExeFileFilter());
        filters.addAll(VirusBufferFileFilter.loadVirusFilters("viruses"));
    }

    /**
     * Запуск процесса выполнения утилиты CopyDiff
     */
    public static void process(Parameters parameters) throws IOException {
        logger.debug("CopyDiff.process[parameters: {}]", parameters);
        // Вызываем рекурсивный обработчик, который пробегает по дереву и вызывает операцию для каждого файла
        new CopyDiff(parameters).processPath(parameters.getSourcePath());
    }

    /**
     * Выполнение процесса обработки одного файла
     */
    private void processFile(Path sourceFile) {
        String relativePath = parameters.getSourcePath().toUri().relativize(sourceFile.toUri()).getPath();
        Parameters fileParams = new Parameters(
                Paths.get(new File(parameters.getSourcePath().toFile(), relativePath).toURI())
               ,Paths.get(new File(parameters.getTargetPath().toFile(), relativePath).toURI())
               ,Paths.get(new File(parameters.getDestPath().toFile(), relativePath).toURI())
        );
        // В случае режима журналирования не ниже DEBUG отображаем информацию по полному имени обрабатываемого файла
        // во всех рабочих папках. В режиме INFO - только сам обрабатываемый файл из папки source_dir
        if (logger.isDebugEnabled()) {
            logger.debug("processFile: [ {} | {} ] -> {}", fileParams.getSourcePath(), fileParams.getTargetPath(), fileParams.getDestPath());
        } else {
            logger.info("processFile: {}", relativePath);
        }
        new CopyDiffFileProcessor(fileParams, filters).process();
    }

    /**
     * Функция рекурсивного обхода по дереву файловой системы от parameters.sourceDir в качестве корня
     */
    private void processPath() {
        processPath(parameters.getSourcePath());
    }

    /**
     * Функция рекурсивного обхода по дереву файловой системы от sourcePath в качестве корня
     */
    private void processPath(Path sourcePath) {
        logger.trace("processPath: {}", sourcePath.getFileName());
        // Если наткнулись на файл, то обрабатываем его
        if (sourcePath.toFile().isFile()) {
            processFile(sourcePath);
        } else if (sourcePath.toFile().isDirectory()) {
            // Если на директорий - вызываем рекурсивно функцию для каждого элемента директория
            for (File file: sourcePath.toFile().listFiles()) {
                processPath(Paths.get(file.toURI()));
            }
        }
    }


}
