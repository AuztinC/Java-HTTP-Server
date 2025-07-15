package Server;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ClientHandlerTest {

    ClientHandler handler;

    @Test
    public void handlesHello() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/hello", "1.1", null, null);
        HttpResponse resp = handler.handle(req);
        assertEquals("200 OK", resp.getStatus());
        assertEquals("1.1", resp.getVersion());
        assertEquals("text/html", resp.getContentType());
        assertEquals(35, resp.getContentLength());
        //... headers, etc
        // hint: you need header content type text/html for the browser to render it
        assertEquals("<html><h1>Hello, World!</h1></html>",
                new String(resp.getBody()));
    }

    @Test
    public void handlesNotFound() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/blah", "1.1", null, null);
        HttpResponse resp = handler.handle(req);
        assertEquals("404 Not Found", resp.getStatus());
    }
}
