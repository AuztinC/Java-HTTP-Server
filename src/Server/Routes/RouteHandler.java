package Server.Routes;

import Server.HTTP.HttpRequest;
import Server.HTTP.HttpResponse;

public interface RouteHandler {

    HttpResponse handle(HttpRequest req);

}
