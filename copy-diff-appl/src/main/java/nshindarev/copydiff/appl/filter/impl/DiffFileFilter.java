package nshindarev.copydiff.appl.filter.impl;

import nshindarev.copydiff.appl.config.Parameters;
import nshindarev.copydiff.appl.filter.Checker;
import nshindarev.copydiff.appl.filter.FileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by nshindarev on 20.08.16.
 */
public class DiffFileFilter implements FileFilter {

    private static final Logger logger = LoggerFactory.getLogger(DiffFileFilter.class);

    // Данный класс проверяет, что файлы source и target совпадают согласно условию задачи

    @Override
    public void check(Parameters parameters, Checker checker) {
        assert checker != null && parameters!= null;
        File sourceFile = parameters.getSourcePath().toFile();
        File targetFile = parameters.getTargetPath().toFile();
        if (!sourceFile.exists()) {
            // Если исходного файла нет - отлуп
            logger.info("Reject: Исходный файл '{}' не существует.", sourceFile.getAbsolutePath());
            checker.reject(this);
        } else if (!sourceFile.isFile()) {
            // Если по пути для исходного файла не файл - отлуп
            logger.info("Reject: По исходному пути '{}' находится не файл.", sourceFile.getAbsolutePath());
            checker.reject(this);
        } else if (!targetFile.exists()) {
            // Если файла для сравнения нет - зелёный свет
            logger.info("Approve: Файл для сравнения '{}' не существует.", targetFile.getAbsolutePath());
            checker.approve(this);
        } else if (!sourceFile.isFile()) {
            // Если по пути файла для сравнения не файл - даём добро
            logger.info("Reject: По пути для сравнения'{}' находится не файл.", targetFile.getAbsolutePath());
            checker.reject(this);
        } else if (sourceFile.lastModified() == targetFile.lastModified() && sourceFile.length() == targetFile.length()) {
            // Если сравниваемые файлы идентичны - отлуп
            logger.info("Reject: Сравниваемые файлы идентичны");
            checker.reject(this);
        } else if (sourceFile.length() != targetFile.length()) {
            // Если поменялась длина - разрешаем копирование
            logger.info("Approve: Сравниваемые файлы имеют разную длину");
            checker.approve(this);
        } else {
            // Если попали сюда, то файлы имеют разное время и одну длину - надо проверить на содержимое (этим будет заниматься
            // фильтр проверки содержимого, когда появится...)
            logger.info("Сравниваемые файлы имеют разную дату. Требуется проверка содержимого.");
        }
    }

}
