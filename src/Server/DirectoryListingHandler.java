package Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DirectoryListingHandler {

    public HttpResponse handleDirectoryListingForImg(HttpRequest req, String root) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        try (Stream<Path> paths = Files.list(Path.of(Path.of(root) + "/img"))) {
            body.write("<html><body><ul>".getBytes());
            paths.forEach(path -> {
                try {
                    String name = path.getFileName().toString();
                    boolean isDir = Files.isDirectory(path);
                    String href = isDir ? req.getPath() + "/" + name : "/" + name;
                    body.write(("<li><a href=\"/img" + href + "\">").getBytes());
                    body.write(name.getBytes());
                    body.write("</a></li>".getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            body.write("</ul></body></html>".getBytes());
            return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
        } catch (IOException e) {
            return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }



    public HttpResponse handleDirectoryListing(HttpRequest req, String root) {
        Path targetDir = Path.of(root, req.getPath().replaceFirst("/listing", "")).normalize();

        if (Files.isDirectory(targetDir)) {
            try (Stream<Path> paths = Files.list(targetDir)) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                body.write("<html><body><ul>".getBytes());

                paths.forEach(path -> {
                    try {
                        String name = path.getFileName().toString();
                        boolean isDir = Files.isDirectory(path);

                        String href = isDir ?
                                req.getPath() + "/" + name :
                                "/" + name;
                        body.write(("<li><a href=\"" + href + "\">" + name + "</a></li>").getBytes());
                    } catch (IOException e) {
//                        throw new UncheckedIOException(e);
                    }
                });

                body.write("</ul></body></html>".getBytes());
                return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
            } catch (IOException e) {
                return new HttpResponse(StatusCode.NOT_FOUND);
            }
        }

        return new HttpResponse(StatusCode.NOT_FOUND);
    }
}
