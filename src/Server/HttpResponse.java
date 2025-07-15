package Server;

import java.util.Map;

public class HttpResponse {

    private final StatusCode statusCode;
    private final ContentType contentType;
    private final Map<StatusCode, String> statuses = Map.of(
            StatusCode.OK, "200 OK",
            StatusCode.NOT_FOUND, "404 Not Found"
    );
    private final byte[] body;

    // pass in status code, body, etc
    public HttpResponse(StatusCode statusCode, ContentType contentType, byte[] body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
    }

    public HttpResponse(StatusCode statusCode) {
        this.statusCode = statusCode;
        this.contentType = null;
        this.body = null;
    }

    public String getStatus() {
        System.out.print(this.statusCode);
        return statuses.get(this.statusCode);
    }

    public String getVersion() {
        return "1.1";
    }

    public byte[] getBody() {
        return this.body;
    }

    public String getContentType() {
        if (this.contentType == ContentType.TEXT_HTML)
            return "text/html";
        return "";
    }

    public int getContentLength() {
        return this.getBody().length;
    }

    public byte[] getBytes() {
        String response = "";
        response += "HTTP/" + this.getVersion() + " " + this.getStatus() + "\r\n";
        response += "Sever: my-http-server\r\n";
        if (body != null) {
            response += "Content-Length: " + this.body.length + "\r\n" +
                    "Content-Type: " + this.getContentType() + "\r\n\r\n" +
                    new String(this.body);
        }

        return response.getBytes();
    }


}
