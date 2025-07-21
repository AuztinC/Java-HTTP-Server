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

    public void start() {
        running = true;
        System.out.println("Austin's Server\n" +
                "Running on port: " + this.port + "\n" +
                "Serving files from: " + this.root);
        Thread thread = new Thread(this::run);
        thread.start();
    }

    private void run() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                Thread t = new Thread(()-> handleClient(client));
                t.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void handleClient(Socket client) {
        try (
                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();) {

            ClientHandler handler = new ClientHandler();
            HttpRequest req = HttpRequest.parse(in);
            HttpResponse resp = handler.handle(req, this.root);
            out.write(resp.getBytes());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void stop() throws IOException {
        running = false;
        serverSocket.close();
    }
}

