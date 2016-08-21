package nshindarev.copydiff.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Created by nshindarev on 17.08.16.
 */
public class ShakespeareTest {

    private static final Logger logger = LoggerFactory.getLogger(ShakespeareTest.class);

    @Test
    public void pathTest() throws Exception {
        checkPath(Shakespeare.path(Shakespeare.sourceRelativePath), Shakespeare.sourceRelativePath);
    }

    @Test
    public void shakespeare() throws Exception {
        checkPath(Shakespeare.shakespeare(), Shakespeare.shakespeareRelativePath);
    }

    private void checkPath(Path path, String relativePath) {
        logger.debug("Пытаемся найти файл '{}'.", relativePath);
        Assert.assertTrue(
                new StringBuilder("Файл ").append(path.toFile().getName()).append(" должен существовать").toString()
                ,path.toFile().exists()
        );
        logger.debug("Файл '{}' успешно найден.", relativePath);
    }

}