package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpRequest {
    private final Methods method;
    private final String path;
    private final String version;

    public HttpRequest(Methods method, String path, String version) {
        this.method = method;
        this.path = path;
        this.version = version;
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

    public static HttpRequest parse(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader((is)));
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

        return new HttpRequest(method, pathString, versionString);
    }


}
