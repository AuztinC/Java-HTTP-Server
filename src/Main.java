import Server.GuessGame.GuessHandler;
import Server.Routes.*;
import Server.Server;
import Server.ServerArgs;
import Server.ThreadSleep;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerArgs config = new ServerArgs(args);
        if (config.isHelpRequested()) {

            System.exit(0);
        }
        Server server = new Server(config);
        server.start();

        server.addRoute("/home", new HomeHandler());
        server.addRoute("/hello", new HelloHandler());
        server.addRoute("/guess", new GuessHandler());
        server.addRoute("/form\\?*.*", new FormHandler());
        server.addRoute("/ping", new PingHandler(new ThreadSleep()));
        server.addRoute("/ping/[0-9]+", new PingHandler(new ThreadSleep()));
        server.addRoute("/listing.*", new DirectoryListingHandler(config.getRoot()));
    }
}