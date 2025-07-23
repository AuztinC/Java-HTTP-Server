package Server.Routes;

import Server.HTTP.HttpRequest;
import Server.HTTP.HttpResponse;
import Server.StatusCode;

public class HelloHandler implements RouteHandler {

    public HttpResponse handle(HttpRequest req) {
        return new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Hello, World!</h1></html>".getBytes());
    }
}
