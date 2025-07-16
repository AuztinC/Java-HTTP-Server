package Server;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpResponseTest {

    @Test
    public void buildsOkResponseWithBody() {
        HttpResponse resp = new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Hello, World!</h1></html>".getBytes());
        String responseBytes = new String(resp.getBytes());
        // add content type length etc as headers here
        assertEquals("HTTP/1.1 200 OK\r\n" +
                "Server: Austin's Server\r\n" +
                "Content-Length: 35\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><h1>Hello, World!</h1></html>", responseBytes);
    }

    @Test
    public void buildsNotFoundResponse() {
        HttpResponse resp = new HttpResponse(StatusCode.NOT_FOUND);
        String responseBytes = new String(resp.getBytes());
        assertEquals("HTTP/1.1 404 Not Found\r\n" +
                "Server: Austin's Server\r\n", responseBytes);
    }


}
