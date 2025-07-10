package Server;

public class ServerArgs {
    int port = 80;
    String root = System.getProperty("user.dir");

    public ServerArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String token = args[i];

            if (token.equals("-p"))
                this.port = Integer.parseInt(args[++i]);

            if (token.equals("-r"))
                this.root = args[++i];
        }
    }

    public int getPort() {
        return this.port;
    }

    public String getRoot() {
        return this.root;
    }
}
