package Server;

import java.nio.file.Path;
import java.util.stream.Stream;

public class TestFileSystem implements FileSystem{
    @Override
    public boolean exists(Path path) {
        return false;
    }

    @Override
    public boolean isDirectory(Path path) {
        return false;
    }

    @Override
    public Stream<Path> list(Path path) {
        return Stream.empty();
    }

    @Override
    public byte[] readAllBytes(Path path) {
        return new byte[0];
    }

    @Override
    public String probeContentType(Path path) {
        return "";
    }
}
