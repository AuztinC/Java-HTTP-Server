package Server;

import java.io.IOException;

public class HttpResponse {

    // pass in status code, body, etc
    public HttpResponse(StatusCode statusCode, ContentType contentType, byte[] bytes) {
    }

    public StatusCode getStatus() {
        return StatusCode.OK;
    }

    public String getVersion() {
        return "1.1";
    }

    public byte[] getBody() {
        return "<html><h1>Hello, World!</h1></html>".getBytes();
    }

    public byte[] getBytes() {
        return "".getBytes();
    }
}
