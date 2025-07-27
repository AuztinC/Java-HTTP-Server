package Server;


import Server.GuessGame.GuessHandler;
import Server.HTTP.HttpRequest;
import Server.HTTP.HttpResponse;
import Server.Routes.*;

import java.util.HashMap;
import java.util.Map;

public class ClientHandler {
    private final Map<String, RouteHandler> routes;
    String root;

    private final StaticFileHandler staticFileHandler;

    public ClientHandler(String root, Map<String, RouteHandler> routes) {
        this.root = root;
        this.routes = routes;

        staticFileHandler = new StaticFileHandler();
    }

    public HttpResponse handle(HttpRequest req) {
        String path = req.getPath();

        if (routes.containsKey(path)) {
            return routes.get(path).handle(req);
        }

        for (String key : routes.keySet()) {
            if (path.matches(key)){
                return routes.get(key).handle(req);
            }
        }

        if (req.getMethod() == Methods.GET)
            return staticFileHandler.handle(req, root);

        return new HttpResponse(StatusCode.NOT_FOUND);
    }

}
