package Server;


import Server.GuessGame.GuessHandler;

public class ClientHandler {
    private final GuessHandler guessHandler = new GuessHandler();
    private final FormHandler formHandler = new FormHandler();
    private final PingHandler pingHandler = new PingHandler();
    private final DirectoryListingHandler directoryListingHandler = new DirectoryListingHandler();
    private final StaticFileHandler staticFileHandler = new StaticFileHandler();

    public HttpResponse handle(HttpRequest req, String root) {

        if (req.getPath().equals("/hello"))
            return handleHello();

        if (req.getPath().startsWith("/form"))
            return formHandler.handle(req);

        if (req.getPath().equals("/guess"))
            return guessHandler.handle(req);

        if (req.getPath().startsWith("/ping"))
            return pingHandler.handle(req);

        if (req.getPath().equals("/listing"))
            return directoryListingHandler.handleDirectoryListing(req, root);

        if (req.getPath().equals("/listing/img"))
            return directoryListingHandler.handleDirectoryListingForImg(req, root);


        if (req.getMethod() == Methods.GET)
            return staticFileHandler.handle(req, root);

        return new HttpResponse(StatusCode.NOT_FOUND);
    }

    private static HttpResponse handleHello() {
        return new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Hello, World!</h1></html>".getBytes());
    }

}