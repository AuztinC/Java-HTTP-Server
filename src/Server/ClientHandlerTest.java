package Server;

import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ClientHandlerTest {

    ClientHandler handler;

    @Test
    public void handlesHello() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/hello", "1.1", null, null);
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));
        assertEquals("200 OK", resp.getStatus());
        assertEquals("1.1", resp.getVersion());
        assertEquals("text/html", resp.getContentType());
        assertEquals(35, resp.getContentLength());
        assertEquals("<html><h1>Hello, World!</h1></html>",
                new String(resp.getBody()));
    }

    @Test
    public void handlesNotFound() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/blah", "1.1", null, null);
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));
        assertEquals("404 Not Found", resp.getStatus());
    }

    @Test
    public void servesProjectFile() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/src/Main.java");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));
        assertEquals("200 OK", resp.getStatus());
        assertEquals("1.1", resp.getVersion());
        assertEquals("text/plain", resp.getContentType());
    }

    @Test
    public void listingsDisplaysFilesInDirectory() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/listing");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir") + "/src/testroot");
        String responseText = new String(resp.getBytes(), StandardCharsets.UTF_8);        assertEquals("200 OK", resp.getStatus());
        assertEquals("1.1", resp.getVersion());
        assertEquals("text/html", resp.getContentType());
        assertTrue(responseText.contains("200 OK"));
        assertTrue(responseText.contains("Content-Type: text/html"));
    }

    @Test
    public void listingImgDisplaysListOfImgFiles() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/listing/img");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir") + "/src/testroot");
        String responseText = new String(resp.getBytes(), StandardCharsets.UTF_8);        assertEquals("200 OK", resp.getStatus());
        assertEquals("1.1", resp.getVersion());
        assertEquals("text/html", resp.getContentType());
        assertTrue(responseText.contains("200 OK"));
        assertTrue(responseText.contains("Content-Type: text/html"));
    }
}
