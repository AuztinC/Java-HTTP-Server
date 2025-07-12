package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final Methods method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(Methods method, String path, String version, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public Methods getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public String getHeader(String name) {
        return headers.get(name.trim().toLowerCase());
    }

    public static HttpRequest parse(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader((is));
        BufferedReader reader = new BufferedReader(isr);
        String requestLine = reader.readLine();


        if (requestLine == null || requestLine.isEmpty())
            throw new IllegalArgumentException("Empty request line");

        String[] parts = requestLine.split(" ");
        if (parts.length != 3)
            throw new IllegalArgumentException("Invalid request line " + requestLine);

        if (parts[1].isEmpty() || !parts[1].contains("/"))
            throw new IllegalArgumentException("Invalid Directory Path " + requestLine);

        if (parts[2].isEmpty() || !parts[2].equals("HTTP/1.1"))
            throw new IllegalArgumentException("Invalid HTTP version " + requestLine);

        String methodString = parts[0];
        String pathString = parts[1];
        String versionString = parts[2];
        Methods method = Methods.confirmMethod(methodString);

        Map<String, String> headers = parseHeaders(reader);

        String contentLength = headers.get("content-length");
        String body = parseBody(contentLength, reader, requestLine);

        return new HttpRequest(method, pathString, versionString, headers, body);
    }

    private static String parseBody(String contentLength, BufferedReader reader, String requestLine) throws IOException {
        if (contentLength != null) {
            int length = Integer.parseInt(contentLength);
            if (length > 0) {
                char[] chars = new char[length];
                int charsRead = reader.read(chars, 0, length);
                if (charsRead < length)
                    throw new IllegalArgumentException("Context Length Mismatch: " + requestLine);

                return new String(chars, 0, charsRead);
            } else
                throw new IllegalArgumentException("Content-Length Cannot Be Negative: " + requestLine);
        }
        return "";
    }

    private static Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        String requestLine;
        Map<String, String> headers = new HashMap<>();
        while ((requestLine = reader.readLine()) != null && !requestLine.isEmpty()) {
            String[] headerParts = requestLine.split(":", 2);

            if (headerParts.length == 2) {
                String name = headerParts[0].toLowerCase();
                String value = headerParts[1].trim();
                headers.put(name, value);
            } else {
                throw new IllegalArgumentException("Malformed header: " + requestLine);
            }
        }
        return headers;
    }


    public String getBody() {
        return body;
    }
}
