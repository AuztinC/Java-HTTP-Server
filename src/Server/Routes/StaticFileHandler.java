package Server.Routes;

import Server.HTTP.HttpRequest;
import Server.HTTP.HttpResponse;
import Server.StatusCode;
import Server.UserFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.nio.file.Path;
import java.util.stream.Stream;

public class StaticFileHandler {
    UserFileSystem userFileSystem = new UserFileSystem();

    public HttpResponse handle(HttpRequest req, String root) {
        Path requestedPath = Path.of(root, req.getPath()).normalize();

        if (userFileSystem.isDirectory(requestedPath)) {
            return serveDirectory(requestedPath, req.getPath());
        }

        if (userFileSystem.exists(requestedPath) && !userFileSystem.isDirectory(requestedPath)) {
            return serveFile(requestedPath);
        }

        return new HttpResponse(StatusCode.NOT_FOUND);
    }

    private HttpResponse serveDirectory(Path dir, String requestPath) {
        Path indexHtml = dir.resolve("index.html");
        if (userFileSystem.exists(indexHtml)) {
            return serveFile(indexHtml);
        }

        try (Stream<Path> paths = userFileSystem.list(dir)) {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            body.write("<html><body><ul>".getBytes());

            paths.forEach(path -> {
                try {
                    String name = path.getFileName().toString();
                    String href = requestPath.endsWith("/") ?
                            requestPath + name :
                            requestPath + "/" + name;
                    body.write(("<li><a href=\"" + href + "\">" + name + "</a></li>").getBytes());
                } catch (IOException ignored) {}
            });

            body.write("</ul></body></html>".getBytes());
            return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());

        } catch (IOException e) {
            return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpResponse serveFile(Path filePath) {
        byte[] body = userFileSystem.readAllBytes(filePath);
        String mimeType = userFileSystem.probeContentType(filePath);
        return new HttpResponse(StatusCode.OK, mimeType, body);
    }

}

