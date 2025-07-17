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
            Path requestedPath = Path.of(root, req.getPath()).normalize();

            if (req.getPath().equals("/hello"))
                return new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Hello, World!</h1></html>".getBytes());

            if (req.getPath().equals("/listing"))
                return handleDirectoryListing(req, root);

            if (req.getPath().equals("/listing/img")) {
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
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            }

            if (Files.isDirectory(requestedPath)) {
                Path indexHtml = requestedPath.resolve("index.html");
                if (Files.exists(indexHtml)) {
                    try {
                        byte[] body = Files.readAllBytes(indexHtml);
                        String mimeType = Files.probeContentType(indexHtml);
                        return new HttpResponse(StatusCode.OK, mimeType, body);
                    } catch (IOException e) {
                        return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    try (Stream<Path> paths = Files.list(requestedPath)) {
                        ByteArrayOutputStream body = new ByteArrayOutputStream();
                        body.write("<html><body><ul>".getBytes());

                        paths.forEach(path -> {
                            try {
                                String name = path.getFileName().toString();
                                String href = req.getPath().endsWith("/") ?
                                        req.getPath() + name :
                                        req.getPath() + "/" + name;
                                body.write(("<li><a href=\"" + href + "\">" + name + "</a></li>").getBytes());
                            } catch (IOException e) {
//                                throw new UncheckedIOException(e);
                            }
                        });

                        body.write("</ul></body></html>".getBytes());
                        return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
                    } catch (IOException e) {
                        return new HttpResponse(StatusCode.NOT_FOUND);
                    }
                }
            }

            if (Files.exists(requestedPath) && !Files.isDirectory(requestedPath)) {
                try {
                    byte[] body = Files.readAllBytes(requestedPath);
                    String mimeType = Files.probeContentType(requestedPath);
                    return new HttpResponse(StatusCode.OK, mimeType, body);
                } catch (IOException e) {
                    return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
                }
            }
            return new HttpResponse(StatusCode.NOT_FOUND);
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

    private HttpResponse handleDirectoryListing(HttpRequest req, String root) {
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