package Server.Routes;

import Server.HTTP.HttpRequest;
import Server.HTTP.HttpResponse;
import Server.StatusCode;

public class HomeHandler implements RouteHandler{
    public HttpResponse handle(HttpRequest req) {
        return new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Welcome Home!</h1></html>".getBytes());
    }
}
