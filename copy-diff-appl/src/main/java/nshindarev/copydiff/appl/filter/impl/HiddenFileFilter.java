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
public class HiddenFileFilter implements FileFilter {

    private static final Logger logger = LoggerFactory.getLogger(HiddenFileFilter.class);

    // Данный класс не даёт скопировать скрытые файлы

    @Override
    public void check(Parameters parameters, Checker checker) {
        assert checker != null && parameters!= null;
        File sourceFile = parameters.getSourcePath().toFile();
        if (sourceFile.isHidden()) {
            // Если файл скрытый - отказываем в копировании
            logger.info("Reject: Исходный файл '{}' является скрытым файлом.", sourceFile.getAbsolutePath());
            checker.reject(this);
        }
    }

}
