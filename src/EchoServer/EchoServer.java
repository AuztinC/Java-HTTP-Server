package EchoServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class EchoServer {

    private static String getResponse() {
        String[] responses = {"HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 11\r\n" +
                "\r\n" +
                "Bad Request",
                "HTTP/1.1 200 Yay!\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 19\r\n" +
                        "\r\n" +
                        "<h1>You da man</h1>"};
        Random rand = new Random();
        return responses[rand.nextInt(responses.length)];
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        while (true) {

            try (
                    ServerSocket serverSocket =
                            new ServerSocket(Integer.parseInt(args[0]));
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out =
                            new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
            ) {
                String inputLine;
                while (!(inputLine = in.readLine()).isEmpty()) {
                    System.out.println(inputLine);
                }
                out.print(getResponse());
                out.flush();
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
                        + portNumber + " or listening for a connection");
                System.out.println(e.getMessage());
            }
        }

    }
}