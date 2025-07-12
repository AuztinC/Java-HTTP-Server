package Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ServerArgsTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void restoreSystemOut() {
        System.setOut(originalOut);
    }

    @Test
    public void parsesDefaultRootDirectory() {
        ServerArgs args = new ServerArgs(new String[]{});
        assertEquals(System.getProperty("user.dir"), args.getRoot());
    }

    @Test
    public void parsesRootDirectory() {
        ServerArgs args = new ServerArgs(new String[]{"-r", "/foo/bar"});
        assertEquals("/foo/bar", args.getRoot());
        assertEquals("Serving files from: /foo/bar",
                outputStream.toString().trim());
    }

    @Test
    public void parsesDefaultValues() {
        ServerArgs args = new ServerArgs(new String[]{});
        assertEquals(80, args.getPort());
    }

    @Test
    public void parsesCustomPort() {
        ServerArgs args = new ServerArgs(new String[]{"-p", "8080"});
        assertEquals(8080, args.getPort());
        assertEquals("Running on port: 8080",
                outputStream.toString().trim());
    }

    @Test
    public void hPrintsHelperMessage() {
        new ServerArgs(new String[]{"-h"});
        assertEquals("Usage: my-http-server [options]\n" +
                        "-p     Specify the port.  Default is 80.\n" +
                        "-r     Specify the root directory.  Default is the current working directory.\n" +
                        "-h     Print this help message\n" +
                        "-x     Print the startup configuration without starting the server",
                outputStream.toString().trim());
    }

    @Test
    public void helpPrintsHelperMessage() {
        new ServerArgs(new String[]{"-help"});
        assertEquals("Usage: my-http-server [options]\n" +
                        "-p     Specify the port.  Default is 80.\n" +
                        "-r     Specify the root directory.  Default is the current working directory.\n" +
                        "-h     Print this help message\n" +
                        "-x     Print the startup configuration without starting the server",
                outputStream.toString().trim());
    }

    @Test
    public void xPrintsConfigWithoutStart() {
        new ServerArgs(new String[]{"-x"});
        assertEquals("Running on port: 80" +
                        "Serving files from: " + System.getProperty("user.dir"),
                outputStream.toString().trim());
    }

    @Test
    public void serverNameStarts() {
        new ServerArgs(new String[]{"my-http-server"});
        assertEquals("<server name>\n" +
                " Running on port: 80\n" +
                " Serving files from: " + System.getProperty("user.dir"), outputStream.toString().trim());
    }


}
