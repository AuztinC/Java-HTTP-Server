package Server;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientHandler {

    public HttpResponse handle(HttpRequest req, String root) {
        if (req.getMethod() == Methods.GET) {
            if (req.getPath().equals("/") || req.getPath().equals("/hello"))
                return new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Hello, World!</h1></html>".getBytes());

            if (req.getPath().equals("/listing")) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                try (Stream<Path> paths = Files.list(Path.of(root))) {
                    body.write("<html><body><ul>".getBytes());
                    paths.forEach(path -> {
                        try {
                            String name = path.getFileName().toString();
                            boolean isDir = Files.isDirectory(path);
                            String href = isDir ? req.getPath() + "/" + name : "/" + name;
                            body.write(("<li><a href=\"" + href + "\">").getBytes());
                            body.write(name.getBytes());
                            body.write("</a></li>".getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    body.write("</ul></body></html>".getBytes());

                    return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
                } catch (IOException e) {
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            }

            if (req.getPath().equals("/listing/img")) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                try (Stream<Path> paths = Files.list(Path.of(Path.of(root) + "/img"))) {
//                    paths.forEach(System.out::println);
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
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            }

            if (req.getPath().startsWith("/img/")) {
                Path path = Path.of(root, req.getPath()); // full path to file
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    try {
                        byte[] body = Files.readAllBytes(path);
                        String mimeType = Files.probeContentType(path);
                        return new HttpResponse(StatusCode.OK, mimeType, body);
                    } catch (IOException e) {
//                        return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            }
        }

        try {
            Path path = Path.of(root, req.getPath());
            byte[] body = Files.readAllBytes(path);
            String mimeType = Files.probeContentType(path);

            return new HttpResponse(StatusCode.OK, mimeType, body);

        } catch (IOException e) {
            return new HttpResponse(StatusCode.NOT_FOUND);
        }
    }
}
