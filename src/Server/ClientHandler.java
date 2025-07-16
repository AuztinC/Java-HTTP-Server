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

    public HttpResponse handle(HttpRequest req) {
        Path directory = Paths.get(System.getProperty("user.dir") + req.getPath());
        if (req.getMethod() == Methods.GET) {
            if (req.getPath().equals("/") || req.getPath().equals("/hello"))
                return new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Hello, World!</h1></html>".getBytes());

            if (req.getPath().equals("/listing")) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                directory = Paths.get(System.getProperty("user.dir") + "/src/testroot");
                try (Stream<Path> paths = Files.list(directory)){
                    body.write("<html><body><ul>".getBytes());
                    paths.forEach(path -> {
                        try {
                            body.write(("<li><a href=\"/src/testroot/" + path.getFileName().toString() + "\">").getBytes());
                            body.write(path.getFileName().toString().getBytes());
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
        }

        try {
            byte[] body = Files.readAllBytes(directory);
            String mimeType = Files.probeContentType(directory);

            return new HttpResponse(StatusCode.OK, mimeType, body);

        } catch (IOException e) {
            return new HttpResponse(StatusCode.NOT_FOUND);
        }
    }
}
