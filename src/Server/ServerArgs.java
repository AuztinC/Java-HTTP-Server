package Server;

public class ServerArgs {
    int port = 80;
    String root = System.getProperty("user.dir");

    public ServerArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String token = args[i];

            if (token.equals("-p")){
                this.port = Integer.parseInt(args[++i]);
                System.out.println("Running on port: " + this.port);
            }

            if (token.equals("-r")){
                this.root = args[++i];
                System.out.println("Serving files from: " + this.root);
            }

            if (token.equals("-h") || token.equals("-help"))
                System.out.println("Usage: my-http-server [options]\n" +
                        "-p     Specify the port.  Default is 80.\n" +
                        "-r     Specify the root directory.  Default is the current working directory.\n" +
                        "-h     Print this help message\n" +
                        "-x     Print the startup configuration without starting the server");

            if (token.equals("-x"))
                System.out.println("Running on port: " + port + "Serving files from: " + root);

            if (token.equals("my-http-server")){
                System.out.println(" <server name>\n" +
                        " Running on port: " + this.port + "\n" +
                        " Serving files from: " + this.root);
                // TODO start server
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
