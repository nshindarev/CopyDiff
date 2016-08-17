package nshindarev.copydiff.appl.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by nshindarev on 19.08.16.
 */
public class Parameters {

    private Path sourceDir;
    private Path targetDir;
    private Path destDir;

    public Parameters() {
    }

    public Parameters(Path sourceDir, Path targetDir, Path destDir ) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
        this.destDir   = destDir;
    }

    public Parameters(String sourceDir, String targetDir, String destDir ) {
        this( sourceDir == null ? null : Paths.get(sourceDir)
             ,targetDir == null ? null : Paths.get(targetDir)
             ,destDir   == null ? null : Paths.get(destDir) );
    }

    public boolean isComplete() {
        return sourceDir != null
            && targetDir != null
            && destDir   != null;
    }

    @Override
    public String toString() {
        return new StringBuilder("source_dir: '").append(sourceDir == null ? "" : sourceDir.toAbsolutePath())
                         .append("', target_dir: '").append(targetDir == null ? "" : targetDir.toAbsolutePath())
                         .append("', dest_dir: '").append(destDir == null ? "" : destDir.toAbsolutePath())
                         .append("'")
                         .toString();
    }

    public final Path getSourceDir() {
        return sourceDir;
    }

    public final void setSourceDir(Path sourceDir) {
        this.sourceDir = sourceDir;
    }

    public final Path getTargetDir() {
        return targetDir;
    }

    public final void setTargetDir(Path targetDir) {
        this.targetDir = targetDir;
    }

    public final Path getDestDir() {
        return destDir;
    }

    public final void setDestDir(Path destDir) {
        this.destDir = destDir;
    }

}