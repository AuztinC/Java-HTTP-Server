package Server;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ClientHandler {

    public HttpResponse handle(HttpRequest req) {
        if (req.getMethod() == Methods.GET)
            if (req.getPath().equals("/"))
                return new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Hello, World!</h1></html>".getBytes());

        try {
            Path path = Paths.get(System.getProperty("user.dir") + req.getPath());
            byte[] body = Files.readAllBytes(path);
            String mimeType = Files.probeContentType(path);
          //  System.out.println(mimeType);

//            List<String> lines = Files.readAllLines(path);
            return new HttpResponse(StatusCode.OK, mimeType, body);

        } catch (IOException e) {
            return new HttpResponse(StatusCode.NOT_FOUND);
        }
    }
}
