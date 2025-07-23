package Server;

import Server.GuessGame.GuessTarget;
import Server.HTTP.HttpRequest;
import Server.HTTP.HttpResponse;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ClientHandlerTest {

    ClientHandler handler;

    @Test
    public void handlesHello() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        HttpRequest req = new HttpRequest(Methods.GET, "/hello", "1.1", null, null);
        HttpResponse resp = handler.handle(req);
        assertEquals("200 OK", resp.getStatus());
        assertEquals("1.1", resp.getVersion());
        assertEquals("text/html", resp.getContentType());
        assertEquals(35, resp.getContentLength());
        assertEquals("<html><h1>Hello, World!</h1></html>",
                new String(resp.getBody()));
    }

    @Test
    public void handlesNotFound() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        HttpRequest req = new HttpRequest(Methods.GET, "/blah", "1.1", null, null);
        HttpResponse resp = handler.handle(req);
        assertEquals("404 Not Found", resp.getStatus());
    }

    @Test
    public void servesProjectFile() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        HttpRequest req = new HttpRequest(Methods.GET, "/src/Main.java");
        HttpResponse resp = handler.handle(req);
        assertEquals("200 OK", resp.getStatus());
        assertEquals("1.1", resp.getVersion());
        assertEquals("text/plain", resp.getContentType());
    }

    @Test
    public void listingsDisplaysFilesInDirectory() {
        handler = new ClientHandler(System.getProperty("user.dir") + "/src/testroot");
        HttpRequest req = new HttpRequest(Methods.GET, "/listing");
        HttpResponse resp = handler.handle(req);
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
        handler = new ClientHandler(System.getProperty("user.dir") + "/src/testroot") ;
        HttpRequest req = new HttpRequest(Methods.GET, "/listing/img");
        HttpResponse resp = handler.handle(req);
        String responseText = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertTrue(responseText.contains("200 OK"));
        assertTrue(responseText.contains("Content-Type: text/html"));
        assertTrue(responseText.contains("<ul>"));
        assertTrue(responseText.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
        assertTrue(responseText.contains("<li><a href=\"/img/decepticon.jpg\">decepticon.jpg</a></li>"));
    }

    @Test
    public void servesIndexHtmlIfPresentInDirectory() {
        handler = new ClientHandler(System.getProperty("user.dir") + "/src/html");
        HttpRequest req = new HttpRequest(Methods.GET, "/");
        HttpResponse resp = handler.handle(req);
        assertEquals("200 OK", resp.getStatus());
        assertEquals("text/html", resp.getContentType());
        assertTrue(new String(resp.getBody()).contains("<p>This index.html is being served by Austin's Server</p>")); // or something more specific from index.html
    }

    @Test
    public void listsDirectoryContentsIfNoIndexHtml() {
        handler = new ClientHandler(System.getProperty("user.dir") + "/src/testroot");
        HttpRequest req = new HttpRequest(Methods.GET, "/img");
        HttpResponse resp = handler.handle(req);

        String responseText = new String(resp.getBytes(), StandardCharsets.UTF_8);

        assertEquals("200 OK", resp.getStatus());
        assertEquals("text/html", resp.getContentType());
        assertTrue(responseText.contains("<ul>"));
        assertTrue(responseText.contains("<li><a href=\"/img/autobot.jpg\">autobot.jpg</a></li>"));
    }

    @Test
    public void formHandlesQueryParams() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        HttpRequest req = new HttpRequest(Methods.GET, "/form?foo=1&bar=2");
        HttpResponse resp = handler.handle(req);

        String responseBody = new String(resp.getBody(), StandardCharsets.UTF_8);

        assertEquals("200 OK", resp.getStatus());
        assertEquals("text/html", resp.getContentType());
        assertTrue(responseBody.contains("<h2>GET Form</h2>"));
        assertTrue(responseBody.contains("<li>foo: 1</li>"));
        assertTrue(responseBody.contains("<li>bar: 2</li>"));
    }

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

        handler = new ClientHandler(System.getProperty("user.dir"));
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", "" + body.getBytes(StandardCharsets.UTF_8).length);
        headers.put("content-type", "multipart/form-data; charset=utf-8; boundary=" + boundary);
        HttpRequest req = new HttpRequest(Methods.POST, "/form", "1.1", headers, body);
        HttpResponse resp = handler.handle(req);

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h2>POST Form</h2>"));
        assertTrue(fullResponse.contains("<li>file name: autobot.jpg</li>"));
        assertTrue(fullResponse.contains("<li>content type: application/octet-stream</li>"));
        assertTrue(fullResponse.contains("<li>file size: 16</li>"));
    }

    @Test
    public void guessGameLandingPageWithCookie() {
        handler = new ClientHandler(System.getProperty("user.dir"));

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", "userId=test-user");

        HttpRequest req = new HttpRequest(Methods.GET, "/guess", "1.1", headers, null);
        HttpResponse resp = handler.handle(req);

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h1>Number Guessing Game</h1>"));
        assertTrue(fullResponse.contains("Input A Number!"));
        assertTrue(fullResponse.contains("You currently have"));
    }

    @Test
    public void guessPOSTReturnPage() {
        String body = "number=10";

        handler = new ClientHandler(System.getProperty("user.dir"));
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", "" + body.getBytes(StandardCharsets.UTF_8).length);
        headers.put("content-type", "text/plain");
        HttpRequest req = new HttpRequest(Methods.POST, "/guess", "1.1", headers, body);
        HttpResponse resp = handler.handle(req);

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h1>Number Guessing Game</h1>"));
    }

    @Test
    public void guessPOSTTooLow() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        String userId = "test-user";

        GuessTarget guessState = GuessTarget.getInstance();
        guessState.setTarget(userId, 50, 7);

        String body = "number=10";
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", String.valueOf(body.length()));
        headers.put("content-type", "application/x-www-form-urlencoded");
        headers.put("cookie", "userId=" + userId);

        HttpRequest req = new HttpRequest(Methods.POST, "/guess", "1.1", headers, body);
        HttpResponse resp = handler.handle(req);
        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);

        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("Too Low!"));
    }

    @Test
    public void guessPOSTTooHigh() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        String userId = "test-user";

        GuessTarget guessState = GuessTarget.getInstance();
        guessState.setTarget(userId, 50, 1);

        String body = "number=90";
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", String.valueOf(body.length()));
        headers.put("content-type", "application/x-www-form-urlencoded");
        headers.put("cookie", "userId=" + userId);

        HttpRequest req = new HttpRequest(Methods.POST, "/guess", "1.1", headers, body);
        HttpResponse resp = handler.handle(req);
        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);

        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("Oops! Better luck next time."));
    }

    @Test
    public void guessPOSTCorrectAnswer() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        String userId = "test-user";

        GuessTarget guessState = GuessTarget.getInstance();
        guessState.setTarget(userId, 50, 1);

        String body = "number=50";
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", String.valueOf(body.length()));
        headers.put("content-type", "application/x-www-form-urlencoded");
        headers.put("cookie", "userId=" + userId);

        HttpRequest req = new HttpRequest(Methods.POST, "/guess", "1.1", headers, body);
        HttpResponse resp = handler.handle(req);
        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);

        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("You got it!"));
    }

    @Test
    public void guessPOSTOutOfGuesses() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        String userId = "test-user";

        GuessTarget guessState = GuessTarget.getInstance();
        guessState.setTarget(userId, 50, 7);

        String body = "number=90";
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", String.valueOf(body.length()));
        headers.put("content-type", "application/x-www-form-urlencoded");
        headers.put("cookie", "userId=" + userId);

        HttpRequest req = new HttpRequest(Methods.POST, "/guess", "1.1", headers, body);
        HttpResponse resp = handler.handle(req);
        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);

        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("Too High!"));
    }

    @Test
    public void pingIsInstant() {
        handler = new ClientHandler(System.getProperty("user.dir"));
        HttpRequest req = new HttpRequest(Methods.GET, "/ping");
        HttpResponse resp = handler.handle(req);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formatted = dateFormat.format(now);

        String fullResponse = new String(resp.getBytes(), StandardCharsets.UTF_8);
        assertEquals("200 OK", resp.getStatus());
        assertTrue(fullResponse.contains("<h2>Ping</h2>"));
        assertTrue(fullResponse.contains("<li>start time: " + formatted + "</li>"));
        assertTrue(fullResponse.contains("<li>end time: " + formatted + "</li>"));
    }



}
