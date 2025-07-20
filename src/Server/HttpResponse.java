package Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class HttpResponse {

    private final StatusCode statusCode;
    private final String contentType;
    private final Map<StatusCode, String> statuses = Map.of(
            StatusCode.OK, "200 OK",
            StatusCode.NOT_FOUND, "404 Not Found",
            StatusCode.INTERNAL_SERVER_ERROR, "500 Internal Server Error"
    );
    private final byte[] body;
    private final byte[] headerBlock;

    public HttpResponse(StatusCode statusCode, String contentType, byte[] body) {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
        this.headerBlock = null;
    }

    public HttpResponse(StatusCode statusCode) {
        this.statusCode = statusCode;
        this.contentType = null;
        this.body = null;
        this.headerBlock = null;
    }

    public HttpResponse(StatusCode statusCode, byte[] headerBlock, byte[] body) {
        this.body = body;
        this.headerBlock = headerBlock;
        this.statusCode = statusCode;
        this.contentType = null;

    }

    public String getStatus() {
        //System.out.print(this.statusCode);
        return statuses.get(this.statusCode);
    }

    public String getVersion() {
        return "1.1";
    }

    public byte[] getBody() {
        return this.body;
    }

    public String getContentType() {
        return this.contentType;
    }

    public int getContentLength() {
        return this.getBody().length;
    }

    public byte[] getBytes() {
        if (headerBlock != null) {
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            try {
                response.write(("HTTP/" + this.getVersion() + " " + this.getStatus() + "\r\n").getBytes());
                response.write(("Server: Austin's Server\r\n").getBytes());
                if (body != null) {
                    response.write(headerBlock);
                    response.write(("Content-Length: " + this.body.length + "\r\n\r\n").getBytes());
                    response.write(this.body);
                }
            } catch (IOException e) {
                return StatusCode.INTERNAL_SERVER_ERROR.toString().getBytes();
            }
            return response.toByteArray();
        }

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        try {
            response.write(("HTTP/" + this.getVersion() + " " + this.getStatus() + "\r\n").getBytes());
            response.write(("Server: Austin's Server\r\n").getBytes());
            if (body != null) {
                response.write(("Content-Length: " + this.body.length + "\r\n" +
                        "Content-Type: " + this.getContentType() + "\r\n\r\n").getBytes());
                response.write(this.body);
            }
        } catch (IOException e) {
            return StatusCode.INTERNAL_SERVER_ERROR.toString().getBytes();
        }
        return response.toByteArray();
    }


}
