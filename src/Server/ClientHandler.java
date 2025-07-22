package Server;


import Server.GuessGame.GuessHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ClientHandler {
    private final Map<String, RouteHandler> routes;
    String root;

    private final StaticFileHandler staticFileHandler;

    public ClientHandler(String root) {
        this.root = root;
        routes = new HashMap<>();

        staticFileHandler = new StaticFileHandler();

        routes.put("/hello", new HelloHandler());
        routes.put("/guess", new GuessHandler());
        routes.put("/form\\?*.*", new FormHandler());
        routes.put("/ping", new PingHandler(new ThreadSleep()));
        routes.put("/ping/[0-9]+", new PingHandler(new ThreadSleep()));
        routes.put("/listing.*", new DirectoryListingHandler(root));
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