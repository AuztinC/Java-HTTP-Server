package Server;

import java.io.File;
import java.io.IOException;

public class ServerArgs {
    int port = 80;
    String root = System.getProperty("user.dir");
    private boolean helpRequested = false;

    public boolean isHelpRequested(){ return helpRequested; }

    public ServerArgs(String[] args) throws IOException {

        for (int i = 0; i < args.length; i++) {
            String token = args[i];

            if (token.equals("-h") || token.equals("-help")) {
                System.out.println(
                        "  -p     Specify the port.  Default is 80.\n" +
                                "  -r     Specify the root directory.  Default is the current working directory.\n" +
                                "  -h     Print this help message\n" +
                                "  -x     Print the startup configuration without starting the server");
                helpRequested = true;
            }

            if (token.equals("-x"))
                helpRequested = true;


            if (token.equals("-p")) {
                if (i + 1 >= args.length)
                    throw new IllegalArgumentException("Expected Port After -p");
                else
                    this.port = Integer.parseInt(args[++i]);

            }

            if (token.equals("-r")) {
                if (i + 1 >= args.length)
                    throw new IllegalArgumentException("Expected Port After -r");
                else
                    this.root = System.getProperty("user.dir") + "/" + args[++i];

            }

        }

        if (helpRequested) {
            System.out.println("Example Server\nRunning on port: "
                    + this.port +
                    "\nServing files from: "
                    + new File(root).getCanonicalPath());
        }
    }

    public int getPort() {
        return this.port;
    }

    public String getRoot() {
        return this.root;
    }
}
