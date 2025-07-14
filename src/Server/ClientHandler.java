package Server;

public class ClientHandler {

    public HttpResponse handle(HttpRequest req) {
        return new HttpResponse(StatusCode.OK, ContentType.TEXT_HTML, new byte[0]);
    }
}
