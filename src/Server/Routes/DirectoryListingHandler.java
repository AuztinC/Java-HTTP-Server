package Server.Routes;

import Server.HTTP.HttpRequest;
import Server.HTTP.HttpResponse;
import Server.StatusCode;
import Server.UserFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DirectoryListingHandler implements RouteHandler {
    String root;

    public DirectoryListingHandler(String root){
        this.root = root;
    }

    public HttpResponse handle(HttpRequest req) {
        UserFileSystem userFileSystem = new UserFileSystem();
        String uri = req.getPath().replaceFirst("/listing", "");

        Path targetDir = Path.of(root, uri).normalize();

        if (userFileSystem.isDirectory(targetDir)) {
            try (Stream<Path> paths = userFileSystem.list(targetDir)) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                body.write("<html><body><ul>".getBytes());

                paths.forEach(path -> {
                    try {
                        String name = path.getFileName().toString();

                        String href = uri + "/" + name;
                        if (userFileSystem.isDirectory(path))
                            href = "/listing" + href;


                        body.write(("<li><a href=\"" + href + "\">" + name + "</a></li>").getBytes());
                    } catch (IOException e) {
//                        throw new UncheckedIOException(e);
                    }
                });

                body.write("</ul></body></html>".getBytes());
                return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
            } catch (IOException e) {
                return new HttpResponse(StatusCode.NOT_FOUND);
            }
        }

        return new HttpResponse(StatusCode.NOT_FOUND);
    }
}
