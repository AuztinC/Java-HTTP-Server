package Server;

import java.nio.file.Files;

public class ClientHandler {

    public HttpResponse handle(HttpRequest req) {
        if (req.getMethod() == Methods.GET && req.getPath().equals("/hello"))
            return new HttpResponse(StatusCode.OK, ContentType.TEXT_HTML, "<html><h1>Hello, World!</h1></html>".getBytes());
         return new HttpResponse(StatusCode.NOT_FOUND);
    }
}
