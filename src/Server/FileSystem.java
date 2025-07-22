package Server;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileSystem {

    boolean exists(Path path);
    boolean isDirectory(Path path);
    Stream<Path> list(Path path);
    byte[] readAllBytes(Path path);
    String probeContentType(Path path);
}
