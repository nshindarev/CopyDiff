package nshindarev.copydiff.test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by nshindarev on 17.08.16.
 *
 * Данный класс используется в фазе тестирования для доступа к файлам проекта
 * в случае, если вызов использован в одном из его поддирректориев.
 *
 */
public class Shakespeare {

    public static final String rootFolderName          = "CopyDiff";
    public static final String shakespeareRelativePath = "/copy-diff-test/src/test/resources/shakespeare.txt";
    public static final String sourceRelativePath      = "/copy-diff-test/src/main/java/nshindarev/copydiff/test/Shakespeare.java";

    public static Path path(String relativePath) {
        assert relativePath != null;
        File path = new File(new File("").getAbsolutePath());
        // Находим файл shakespeare.txt в тестовом модуле
        while (!path.getName().equals(rootFolderName) && path.getParent() != null) {
            path = path.getParentFile();
        }
        return Paths.get(new StringBuilder(path.getAbsolutePath()).append(relativePath).toString());
    }

    public static Path shakespeare() {
        return path(shakespeareRelativePath);
    }


}
