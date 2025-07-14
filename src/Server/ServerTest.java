package Server;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerTest {
    Server server;
    ServerArgs args;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void tearDown() throws IOException {
        if (server != null && server.getServerSocket() != null && !server.getServerSocket().isClosed()) {
            server.getServerSocket().close();
        }
        outputStream.reset();
    }

    @Test
    public void startsServerOnSpecifiedPort() throws IOException {
        args = new ServerArgs(new String[]{"-p", "8080"});
        server = new Server(args);
        assertEquals(8080, server.getPort());
    }

    @Test
    public void createsServerSocketOnPort() throws IOException {
        args = new ServerArgs(new String[]{"-p", "8080"});
        server = new Server(args);
        ServerSocket socket = server.getServerSocket();
        assertEquals(8080, socket.getLocalPort());
        server.getServerSocket().close();
    }

}
