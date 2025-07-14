package Server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FakeClientSocket implements ClientSocket {
    private final InputStream input;
    private final OutputStream output;

    FakeClientSocket(String request) {
        this.input = new ByteArrayInputStream(request.getBytes());
        this.output = new ByteArrayOutputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return this.output;
    }

    @Override
    public InputStream getInputStream() {
        return this.input;
    }
}
