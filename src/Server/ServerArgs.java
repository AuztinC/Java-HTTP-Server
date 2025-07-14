package Server;

import java.io.File;
import java.io.IOException;

public class ServerArgs {
    int port = 80;
    String root = System.getProperty("user.dir");

    public ServerArgs(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            String token = args[i];

            if (token.equals("-p")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Expected Port After -p");
                } else {
                    this.port = Integer.parseInt(args[++i]);
                    System.out.println("Running on port: " + this.port);
                }
            }

            if (token.equals("-r")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Expected Port After -r");
                } else {
                    this.root = args[++i];
                    System.out.println("Serving files from: " + new File(root).getCanonicalPath());

                }
            }

            if (token.equals("-h") || token.equals("-help")) {
                System.out.println("Usage: my-http-server [options]\n" +
                        "-p     Specify the port.  Default is 80.\n" +
                        "-r     Specify the root directory.  Default is the current working directory.\n" +
                        "-h     Print this help message\n" +
                        "-x     Print the startup configuration without starting the server");
                return;
            }

            if (token.equals("-x")) {
                System.out.println("Example Server\nRunning on port: " + port + "\nServing files from: " + new File(root).getCanonicalPath());
                return;
            }

        }
    }

    public int getPort() {
        return this.port;
    }

    public String getRoot() {
        return this.root;
    }
}
