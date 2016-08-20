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

    // Находим файл по заданному relativePath от корня проекта
    public static Path path(String relativePath) {
        assert relativePath != null;
        File currentPath = new File("");
        // Бежим по дереву проекта от текущего директория вверх пока не найдём корень проекта или не получим null
        for (File path = new File(currentPath.getAbsolutePath()); path != null; path = path.getParentFile()) {
            // Если имя проекта не изменилось и мы стоим на папке проекта
            // или если текущая папка содержит папку модуля 'copy-difff-test',
            // то выставляем текущую папку как искомую
            if ( path.getName().equals("CopyDiff") ||
                 new File(path, "copy-diff-test").isDirectory() )
            {
                currentPath = path;
                break;
            }
        }
        // Если не нашли - пытаемся
        return Paths.get(new StringBuilder(currentPath.getAbsolutePath()).append(relativePath).toString());
    }

    // Находим файл shakespeare.txt в тестовом модуле
    public static Path shakespeare() {
        return path(shakespeareRelativePath);
    }


}
