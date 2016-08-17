package nshindarev.copydiff.appl.service;

import nshindarev.copydiff.appl.config.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nshindarev on 17.08.16.
 */
public class CopyDiff {

    private static final Logger logger = LoggerFactory.getLogger(CopyDiff.class);

    private Parameters parameters;

    /**
     * Конструктор скрыт. Доступ производится через static метод обработки
     * @param parameters
     */
    private CopyDiff(Parameters parameters) {
        // Предполагается, что без заполненных параметров сюда соваться не надо
        assert parameters != null && parameters.isComplete();
        this.parameters = parameters;
    }

    /**
     * Запуск процесса выполнения утилиты CopyDiff
     */
    public static void process(Parameters parameters) {
        logger.debug("CopyDiff.process[parameters: {}]", parameters);
        // Вызываем рекурсивный обработчик, который пробегает по дереву и вызывает операцию для каждого файла
        new CopyDiff(parameters).processPath(parameters.getSourceDir());
    }

    /**
     * Выполнение процесса обработки одного файла
     */
    private void processFile(Path sourceFile) {
        String relativePath = parameters.getSourceDir().toUri().relativize(sourceFile.toUri()).getPath();
        // В случае режима журналирования не ниже DEBUG отображаем информацию по полному имени обрабатываемого файла
        // во всех рабочих папках. В режиме INFO - только сам обрабатываемый файл из папки source_dir
        if (logger.isDebugEnabled()) {
            List<String> files = new ArrayList<>();
            for (Path path:new Path[] {parameters.getSourceDir(), parameters.getTargetDir(), parameters.getDestDir()}) {
                files.add(new File(path.toFile(), relativePath).getAbsolutePath());
            }
            logger.debug("processFile: [ {} | {} ] -> {}", files.get(0), files.get(1), files.get(2));
        } else {
            logger.info("processFile: {}", relativePath);
        }
    }

    /**
     * Функция рекурсивного обхода по дереву файловой системы от parameters.sourceDir в качестве корня
     */
    private void processPath() {
        processPath(parameters.getSourceDir());
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
