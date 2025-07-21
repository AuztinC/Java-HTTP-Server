import Server.Server;
import Server.ServerArgs;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerArgs config = new ServerArgs(args);
        if (config.isHelpRequested()) {

            System.exit(0);
        }
        Server server = new Server(config);
        server.start();
    }
}