package Server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    private final ServerSocket serverSocket;
    private final int port;
    private final String root;
    private boolean running;

    public Server(ServerArgs args) throws IOException {
        this.port = args.port;
        this.serverSocket = new ServerSocket(this.port);
        this.root = args.root;
    }

    public int getPort() {
        return port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void start() throws IOException {
        running = true;
        System.out.println("Example Server\n" +
                "Running on port: " + this.port + "\n" +
                "Serving files from: " + this.root);
        System.out.flush();
        while (running) {
            try (Socket client = serverSocket.accept();
                 InputStream in = client.getInputStream();
                 OutputStream out = client.getOutputStream();) {
                HttpRequest parsedInput = HttpRequest.parse(in);
                System.out.println(parsedInput.getMethod());

                while (parsedInput.getMethod() != null) {
                    System.out.println(parsedInput.getMethod());

                }
            }
        }

    }

    public void stop() throws IOException {
        running = false;
        serverSocket.close();
    }
}

