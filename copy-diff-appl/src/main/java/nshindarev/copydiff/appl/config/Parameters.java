package nshindarev.copydiff.appl.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by nshindarev on 19.08.16.
 */
public class Parameters {

    // Соответственно source, target и dest пути. Могут использоваться
    // как для задания пути так и для задания конечных файлов.
    private Path sourcePath;
    private Path targetPath;
    private Path destPath;

    public Parameters() {
    }

    public Parameters(Path sourcePath, Path targetPath, Path destPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.destPath = destPath;
    }

    public Parameters(String sourcePath, String targetPath, String destPath) {
        this( sourcePath == null ? null : Paths.get(sourcePath)
             , targetPath == null ? null : Paths.get(targetPath)
             , destPath == null ? null : Paths.get(destPath) );
    }

    public boolean isComplete() {
        return sourcePath != null
            && targetPath != null
            && destPath   != null;
    }

    @Override
    public String toString() {
        return new StringBuilder("sourcePath: '").append(sourcePath == null ? "" : sourcePath.toAbsolutePath())
                         .append("', targetPath: '").append(targetPath == null ? "" : targetPath.toAbsolutePath())
                         .append("', destPath: '").append(destPath == null ? "" : destPath.toAbsolutePath())
                         .append("'")
                         .toString();
    }

    public final Path getSourcePath() {
        return sourcePath;
    }

    public final void setSourcePath(Path sourcePath) {
        this.sourcePath = sourcePath;
    }

    public final Path getTargetPath() {
        return targetPath;
    }

    public final void setTargetPath(Path targetPath) {
        this.targetPath = targetPath;
    }

    public final Path getDestPath() {
        return destPath;
    }

    public final void setDestPath(Path destPath) {
        this.destPath = destPath;
    }

}