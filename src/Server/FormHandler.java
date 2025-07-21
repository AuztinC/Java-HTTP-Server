package Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FormHandler {

    public HttpResponse handle(HttpRequest req) {
        if (req.getMethod() == Methods.GET && req.getPath().contains("?")) {
            return handleGetForm(req);
        }

        String contentType = req.getHeader("content-type");
        if (req.getMethod() == Methods.POST &&
                contentType != null &&
                contentType.contains("multipart/form-data")) {
            return handleMultipartForm(req);
        }

        return new HttpResponse(StatusCode.NOT_FOUND);
    }

    private HttpResponse handleGetForm(HttpRequest req) {
        List<String> pathString = List.of(req.getPath().substring(6).split("&"));
        Map<String, String> queryParams = pathString.stream()
                .map(s -> s.split("=", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

        try {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            body.write("<html><body><h2>GET Form</h2><ul>".getBytes());
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                body.write(("<li>" + entry.getKey() + ": " + entry.getValue() + "</li>").getBytes());
            }
            body.write("</ul></body></html>".getBytes());
            return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
        } catch (IOException e) {
            return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpResponse handleMultipartForm(HttpRequest req) {
        String contentType = req.getHeader("content-type");
        String boundary = Arrays.stream(contentType.split(";"))
                .map(String::trim)
                .filter(s -> s.startsWith("boundary="))
                .map(s -> s.substring("boundary=".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No boundary in content-type"));

        List<Multipart> parts = ClientHandler.parseMultipart(req.getBody(), boundary);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        ByteArrayOutputStream headers = new ByteArrayOutputStream();

        try {
            body.write("<html><body><h2>POST Form</h2><ul>".getBytes());
            for (Multipart part : parts) {
                body.write(("<li>file name: " + part.filename() + "</li>").getBytes());
                body.write(("<li>content type: " + part.contentType() + "</li>").getBytes());
                body.write(("<li>file size: " + part.content().length + "</li>").getBytes());
            }
            body.write("</ul></body></html>".getBytes());
            return new HttpResponse(StatusCode.OK, headers.toByteArray(), body.toByteArray());
        } catch (IOException e) {
            return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }
}
