package Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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
    public void parsesDefaultRootDirectory() throws IOException {
        ServerArgs args = new ServerArgs(new String[]{});
        assertEquals(System.getProperty("user.dir"), args.getRoot());
    }

    @Test
    public void parsesRootDirectory() throws IOException {
        ServerArgs args = new ServerArgs(new String[]{"-r", "/foo/bar"});
        assertEquals("/foo/bar", args.getRoot());
        assertEquals("Serving files from: /foo/bar",
                outputStream.toString().trim());
    }
    @Test
    public void throwForRootFlagWithoutValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ServerArgs(new String[]{"-r"});
        });
    }


    @Test
    public void parsesDefaultValues() throws IOException {
        ServerArgs args = new ServerArgs(new String[]{});
        assertEquals(80, args.getPort());
    }

    @Test
    public void parsesCustomPort() throws IOException {
        ServerArgs args = new ServerArgs(new String[]{"-p", "8080"});
        assertEquals(8080, args.getPort());
        assertEquals("Running on port: 8080",
                outputStream.toString().trim());
    }

    @Test
    public void throwForPortFlagWithoutValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ServerArgs(new String[]{"-p"});
        });
    }

    @Test
    public void hPrintsHelperMessage() throws IOException {
        new ServerArgs(new String[]{"-h"});
        assertEquals("Usage: my-http-server [options]\n" +
                        "-p     Specify the port.  Default is 80.\n" +
                        "-r     Specify the root directory.  Default is the current working directory.\n" +
                        "-h     Print this help message\n" +
                        "-x     Print the startup configuration without starting the server",
                outputStream.toString().trim());
    }

    @Test
    public void helpPrintsHelperMessage() throws IOException {
        new ServerArgs(new String[]{"-help"});
        assertEquals("Usage: my-http-server [options]\n" +
                        "-p     Specify the port.  Default is 80.\n" +
                        "-r     Specify the root directory.  Default is the current working directory.\n" +
                        "-h     Print this help message\n" +
                        "-x     Print the startup configuration without starting the server",
                outputStream.toString().trim());
    }

    @Test
    public void xPrintsConfigWithoutStart() throws IOException {
        new ServerArgs(new String[]{"-x"});
        assertEquals("Example Server\nRunning on port: 80" +
                        "\nServing files from: " + System.getProperty("user.dir"),
                outputStream.toString().trim());
    }


    @Test
    public void updatedServerConfigWithFlags() throws IOException {
        new ServerArgs(new String[]{"-p", "8080", "-r", "/home"});
        assertEquals("Running on port: 8080\n" +
                "Serving files from: " + new File("/home").getCanonicalPath(), outputStream.toString().trim());
    }
}
