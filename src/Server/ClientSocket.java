package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ClientSocket {
    OutputStream getOutputStream() throws IOException;
    InputStream getInputStream() throws IOException;
}
