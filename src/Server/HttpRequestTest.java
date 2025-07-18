package Server;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

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
    public void checksForGETMethod() throws IOException {
        InputStream is = new ByteArrayInputStream("GET / HTTP/1.1\r\n\r\n".getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals(Methods.GET, req.getMethod());
    }

    @Test
    public void checksForPUTMethod() throws IOException {
        InputStream is = new ByteArrayInputStream("PUT / HTTP/1.1\r\n\r\n".getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals(Methods.PUT, req.getMethod());
    }

    @Test
    public void checksForPOSTMethod() throws IOException {
        InputStream is = new ByteArrayInputStream("POST / HTTP/1.1\r\n\r\n".getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals(Methods.POST, req.getMethod());
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

    @Test
    public void parsesHeaderFields() throws IOException {
        String rawRequest = "GET /hello HTTP/1.1\r\n"
                + "Host: example.com\r\n"
                + "User-Agent: Mozilla/5.0\r\n"
                + "Accept: */*\r\n";

        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        HttpRequest req = HttpRequest.parse(is);

        assertEquals("example.com", req.getHeader("host"));
        assertEquals("Mozilla/5.0", req.getHeader("user-agent"));
    }

    @Test
    public void throwsForContentLengthWithString() {
        String rawRequest = "POST / HTTP/1.1\r\n"
                + "Host: example.com\r\n"
                + "User-Agent: Mozilla/5.0\r\n"
                + "Accept: */*\r\n"
                + "Content-Length: number\r\n\r\n";
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void throwsForNegativeContentLength() {
        String rawRequest = "POST / HTTP/1.1\r\n"
                + "Host: example.com\r\n"
                + "User-Agent: Mozilla/5.0\r\n"
                + "Accept: */*\r\n"
                + "Content-Length: -1\r\n\r\n";
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void throwsForContentLengthWithNoBody() {
        String rawRequest = "POST / HTTP/1.1\r\n"
                + "Host: example.com\r\n"
                + "User-Agent: Mozilla/5.0\r\n"
                + "Accept: */*\r\n"
                + "Content-Length: 1\r\n\r\n";
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void readsBodyContent() throws IOException {
        String rawRequest = "POST / HTTP/1.1\r\n"
                + "Host: example.com\r\n"
                + "User-Agent: Mozilla/5.0\r\n"
                + "Accept: */*\r\n"
                + "Content-Length: 1\r\n\r\n"
                + "123";

        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals("1", req.getBody());
    }

    @Test
    public void handlesBodyWithNewLine() throws IOException {
        String rawRequest = "POST / HTTP/1.1\r\n"
                + "Host: example.com\r\n"
                + "User-Agent: Mozilla/5.0\r\n"
                + "Accept: */*\r\n"
                + "Content-Length: 17\r\n\r\n"
                + "12345\nHellow Rodl";

        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertEquals("12345\nHellow Rodl", req.getBody());
    }

    @Test
    public void throwsForTooLongContentLength() throws IOException {
        String rawRequest = "POST / HTTP/1.1\r\n"
                + "Host: example.com\r\n"
                + "User-Agent: Mozilla/5.0\r\n"
                + "Accept: */*\r\n"
                + "Content-Length: 20\r\n\r\n"
                + "12345\nHellow Rodl";

        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            HttpRequest.parse(is);
        });
    }

    @Test
    public void readMultiPartPOST() throws IOException {
        String boundary = "----MyBoundary";
        String body =
                "------" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"autobot.jpg\"\r\n" +
                        "Content-Type: application/octet-stream\r\n" +
                        "\r\n" +
                        "FAKEIMAGECONTENT" + "\r\n" +
                        "------" + boundary + "--\r\n";

        String rawRequest =
                "POST / HTTP/1.1\r\n" +
                        "Host: example.com\r\n" +
                        "User-Agent: TestClient\r\n" +
                        "Content-Type: multipart/form-data; boundary=" + boundary + "\r\n" +
                        "Content-Length: " + body.getBytes().length + "\r\n" +
                        "\r\n" +
                        body;

        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        HttpRequest req = HttpRequest.parse(is);
        assertTrue(req.getBody().contains("MyBoundary--"));
        assertTrue(req.getBody().contains("Content-Type: application/octet-stream"));
    }

}
