package Server;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpResponseTest {

    @Test
    public void buildsOkResponseWithBody() {
        HttpResponse resp = new HttpResponse(StatusCode.OK, ContentType.TEXT_HTML, "<html><h1>Hello, World!</h1></html>".getBytes());
        String responseBytes = new String(resp.getBytes());
        // add content type length etc as headers here
        assertEquals("HTTP/1.1 200 OK\r\n\r\n<html><h1>Hello, World!</h1></html>", responseBytes);
    }

//    @Test
//    public void respondsToHelloPath() throws IOException {
//        String rawRequest = "GET /hello HTTP/1.1\r\n"
//                + "Host: example.com\r\n"
//                + "User-Agent: Mozilla/5.0\r\n"
//                + "Accept: */*\r\n\r\n";
//
//        FakeClientSocket socket = new FakeClientSocket(rawRequest);
//        HttpResponse handler = new HttpResponse(socket);
//        System.out.println(socket.getInputStream());
//        String output = socket.getOutputStream().toString();
//        assertTrue(output.contains("HTTP/1.1 200 OK"));
//    }
}
