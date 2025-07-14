import Server.Server;
import Server.ServerArgs;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerArgs config = new ServerArgs(args);
        Server server = new Server(config);
        server.start();
    }
}