package nshindarev.copydiff.appl;

import nshindarev.copydiff.appl.config.Parameters;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by nshindarev on 16.08.16.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Application started.");
        Parameters parameters = parseParameters(args);
        if (parameters != null) {
        }
        logger.info("Application completed.");
    }

    private static final String sourceDirParam = "source_dir";
    private static final String targetDirParam = "target_dir";
    private static final String destDirParam   = "dest_dir";
    private static final String helpParam      = "help";

    private static Parameters parseParameters(String... args) {

        Options options = new Options();

        options.addOption(sourceDirParam, true, "Путь, содержащий исходную сравниваемую структуру файлов");
        options.addOption(targetDirParam, true, "Путь, содержащий структуру файлов с которой сравниваем исходную");
        options.addOption(destDirParam,   true, "Путь по которому будут сохранены итоговые файлы");
        options.addOption(helpParam,            "Отобразить информацию о параметрах запуска приложения");

        Parameters parameters = new Parameters();
        boolean params = true;
        try {
            CommandLine cl = new DefaultParser().parse(options, args);
            for (String dir : new String[] {sourceDirParam, targetDirParam, destDirParam}) {
                if (!cl.hasOption(dir)) {
                    logger.error("Не задан параметр {}", dir);
                    params = false;
                }
            }
            if (params) {
                // sourceDir
                Path sourceDir = Paths.get(cl.getOptionValue(sourceDirParam));
                if (Files.notExists(sourceDir)) {
                    logger.warn("Не существует папка, заданная параметром {}: '{}'", sourceDirParam, cl.getOptionValue(sourceDirParam));
                }
                parameters.setSourceDir(sourceDir);
                // destDir
                Path destDir = Paths.get(cl.getOptionValue(destDirParam));
                if (!Files.exists(destDir)) {
                    if (destDir.toFile().mkdirs()) {
                        logger.info("Cоздана папка для {}: '{}'", destDirParam, cl.getOptionValue(destDirParam));
                    } else {
                        logger.error("Не удалось создать папку {}: '{}'", destDirParam, cl.getOptionValue(destDirParam));
                        destDir = null;
                    }
                }
                parameters.setDestDir(destDir);
                parameters.setTargetDir(Paths.get(cl.getOptionValue(targetDirParam)));
            }
            if (cl.hasOption("h") || cl.hasOption("help") || !params) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(132);
                helpFormatter.printHelp(" аргументы для запуска утилиты", options);
                return null;
            }
        } catch (ParseException pe) {
            logger.error("Не удаётся разобрать строку параметров", pe);
        }
        logger.debug("Parameters: {}", parameters.toString());
        return parameters.isComplete() ? parameters : null;
    }

}
