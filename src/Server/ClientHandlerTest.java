package Server;

import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        String responseText = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertTrue(responseText.contains("200 OK"));
        assertTrue(responseText.contains("Content-Type: text/html"));
        assertTrue(responseText.contains("<ul>"));
        assertTrue(responseText.contains("<li><a href=\"/index.html\">index.html</a></li>"));
        assertTrue(responseText.contains("<li><a href=\"/hello.pdf\">hello.pdf</a></li>"));
        assertTrue(responseText.contains("<li><a href=\"/listing/img\">img</a></li>"));
    }

    @Test
    public void listingImgDisplaysListOfImgFiles() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/listing/img");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir") + "/src/testroot");
        String responseText = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertTrue(responseText.contains("200 OK"));
        assertTrue(responseText.contains("Content-Type: text/html"));
        assertTrue(responseText.contains("<ul>"));
        assertTrue(responseText.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
        assertTrue(responseText.contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    }

    @Test
    public void servesIndexHtmlIfPresentInDirectory() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir") + "/src/html");
        assertEquals("200 OK", resp.getStatus());
        assertEquals("text/html", resp.getContentType());
        assertTrue(new String(resp.getBody()).contains("<p>This index.html is being served by Austin's Server</p>")); // or something more specific from index.html
    }
    @Test
    public void listsDirectoryContentsIfNoIndexHtml() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/img");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir") + "/src/testroot");

        String responseText = new String(resp.getBytes(), StandardCharsets.UTF_8);

        assertEquals("200 OK", resp.getStatus());
        assertEquals("text/html", resp.getContentType());
        assertTrue(responseText.contains("<ul>"));
        assertTrue(responseText.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    }

    @Test
    public void formHandlesQueryParams() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/form?foo=1&bar=2");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));

        String responseBody = new String(resp.getBody(), StandardCharsets.UTF_8);

        assertEquals("200 OK", resp.getStatus());
        assertEquals("text/html", resp.getContentType());
        assertTrue(responseBody.contains("<h2>GET Form</h2>"));
        assertTrue(responseBody.contains("<li>foo: 1</li>"));
        assertTrue(responseBody.contains("<li>bar: 2</li>"));
    }

///form handles post multipart form with file upload
    @Test
    public void formPostMultiPartUpload() {
        String boundary = "----MyBoundary";
        String body =
                "------" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"autobot.jpg\"\r\n" +
                        "Content-Type: application/octet-stream\r\n" +
                        "\r\n" +
                        "FAKEIMAGECONTENT" + "\r\n" +
                        "------" + boundary + "--\r\n";

        handler = new ClientHandler();
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", "" + body.getBytes(StandardCharsets.UTF_8).length);
        headers.put("content-type", "multipart/form-data; charset=utf-8; boundary=" + boundary);
        HttpRequest req = new HttpRequest(Methods.POST, "/form", "1.1", headers, body);
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h2>POST Form</h2>"));
        assertTrue(fullResponse.contains("<li>file name: autobot.jpg</li>"));
        assertTrue(fullResponse.contains("<li>content type: application/octet-stream</li>"));
        assertTrue(fullResponse.contains("<li>file size: 16</li>"));
    }

//    @Test
//    public void guessGameLandingPageWithCookie() {
//        handler = new ClientHandler();
//        HttpRequest req = new HttpRequest(Methods.GET, "/guess");
//        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));
//
//        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
//        assertEquals("200 OK", resp.getStatus());
//        assertTrue(fullResponse.contains("<h1>Number Guessing Game</h1>"));
//    }

    @Test
    public void guessPOSTCompareNumber() {
        String body = "number=10";

        handler = new ClientHandler();
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", "" + body.getBytes(StandardCharsets.UTF_8).length);
        headers.put("content-type", "text/plain");
        HttpRequest req = new HttpRequest(Methods.POST, "/guess", "1.1", headers, body);
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h1>Number Guessing Game</h1>"));
    }

    @Test
    public void pingIsInstant() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/ping");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formatted = dateFormat.format(now);

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h2>Ping</h2>"));
        assertTrue(fullResponse.contains("<li>start time: " + formatted + "</li>"));
        assertTrue(fullResponse.contains("<li>end time: " + formatted + "</li>"));
    }

    @Test
    public void pingOneWaitsOneSecond() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/ping/1");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusSeconds(1);

        String formattedNow = dateFormat.format(now);
        String formattedLater = dateFormat.format(later);

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h2>Ping</h2>"));
        assertTrue(fullResponse.contains("<li>start time: " + formattedNow + "</li>"));
        assertTrue(fullResponse.contains("<li>end time: " + formattedLater + "</li>"));
    }

    @Test
    public void pingTwoWaitsTwoSecond() {
        handler = new ClientHandler();
        HttpRequest req = new HttpRequest(Methods.GET, "/ping/2");
        HttpResponse resp = handler.handle(req, System.getProperty("user.dir"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusSeconds(2);

        String formattedNow = dateFormat.format(now);
        String formattedLater = dateFormat.format(later);

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h2>Ping</h2>"));
        assertTrue(fullResponse.contains("<li>start time: " + formattedNow + "</li>"));
        assertTrue(fullResponse.contains("<li>end time: " + formattedLater + "</li>"));
    }



}
