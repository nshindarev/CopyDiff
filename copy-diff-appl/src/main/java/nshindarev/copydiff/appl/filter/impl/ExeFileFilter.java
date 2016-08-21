package nshindarev.copydiff.appl.filter.impl;

import nshindarev.copydiff.appl.config.Parameters;
import nshindarev.copydiff.appl.filter.Checker;
import nshindarev.copydiff.appl.filter.ContinueFilter;
import nshindarev.copydiff.appl.filter.FileFilter;
import nshindarev.copydiff.appl.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by nshindarev on 20.08.16.
 */
public class ExeFileFilter implements FileFilter {

    private static final Logger logger = LoggerFactory.getLogger(ExeFileFilter.class);

    // Данный класс не даёт скопировать '.exe' файлы

    @Override
    public ContinueFilter check(Parameters parameters, Checker checker) {
        assert checker != null && parameters!= null;
        if (parameters.getSourcePath().toFile().getName().toLowerCase().matches("^.*\\.exe$")) {
            logger.info("Reject: EXE файлы не допускаются. Файл '{}' не будет скопирован.", parameters.getSourcePath());
            checker.reject(this);
        }
        return null;
    }

}
