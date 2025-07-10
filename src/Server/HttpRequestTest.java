package Server;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class HttpRequestTest {

    @Test
    public void throwsWhenGivenNoInput() {
        InputStream is = new ByteArrayInputStream("".getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void throwsWhenGivenBadInput() {
        InputStream is = new ByteArrayInputStream("BLAH".getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void acceptsPathWithSubdirectories() throws IOException {
        InputStream is = new ByteArrayInputStream("GET /guess/game HTTP/1.1\r\n\r\n".getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals(Methods.GET, req.getMethod());
    }

    @Test
    public void throwsWithoutThreeInputParts() {
        InputStream is = new ByteArrayInputStream("GET HTTP/1.1\r\n\r\n".getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void checksForGetMethod() throws IOException {
        InputStream is = new ByteArrayInputStream("GET / HTTP/1.1\r\n\r\n".getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals(Methods.GET, req.getMethod());
    }

    @Test
    public void checksForPutMethod() throws IOException {
        InputStream is = new ByteArrayInputStream("PUT / HTTP/1.1\r\n\r\n".getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals(Methods.PUT, req.getMethod());
    }

    @Test
    public void throwsForDirectoryWithoutSlash() {
        InputStream is = new ByteArrayInputStream("GET word HTTP/1.1\r\n\r\n".getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void extractsVersion() throws IOException {
        InputStream is = new ByteArrayInputStream("GET /ping HTTP/1.1\r\n\r\n".getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals("HTTP/1.1", req.getVersion());
    }

    @Test
    public void throwsForBadHTTPVersion() {
        InputStream is = new ByteArrayInputStream("GET / HTTP/1.0\r\n\r\n".getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void extractsRequestPath() throws IOException {
        InputStream is = new ByteArrayInputStream("GET /ping HTTP/1.1\r\n\r\n".getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals("/ping", req.getPath());
    }


}
