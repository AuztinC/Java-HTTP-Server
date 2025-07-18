package Server;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientHandler {

    public HttpResponse handle(HttpRequest req, String root) {
        if (req.getMethod() == Methods.GET) {
            Path requestedPath = Path.of(root, req.getPath()).normalize();

            if (req.getPath().equals("/hello"))
                return new HttpResponse(StatusCode.OK, "text/html", "<html><h1>Hello, World!</h1></html>".getBytes());

            if (req.getPath().equals("/listing"))
                return handleDirectoryListing(req, root);

            if (req.getPath().equals("/listing/img")) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                try (Stream<Path> paths = Files.list(Path.of(Path.of(root) + "/img"))) {
                    body.write("<html><body><ul>".getBytes());
                    paths.forEach(path -> {
                        try {
                            String name = path.getFileName().toString();
                            boolean isDir = Files.isDirectory(path);
                            String href = isDir ? req.getPath() + "/" + name : "/" + name;
                            body.write(("<li><a href=\"/img" + href + "\">").getBytes());
                            body.write(name.getBytes());
                            body.write("</a></li>".getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    body.write("</ul></body></html>".getBytes());
                    return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
                } catch (IOException e) {
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            }

            if (req.getPath().contains("/form?")) {
                List<String> substr = List.of(req.getPath().substring(6).split("&"));
                Map<String, String> queryParams = substr.stream()
                        .map(s -> s.split("=", 2))
                        .filter(arr -> arr.length == 2)
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

                try {
                    ByteArrayOutputStream body = new ByteArrayOutputStream();
                    body.write("<html><body><h2>GET Form</h2><ul>".getBytes());

                    queryParams.forEach((k, v) -> {
                        try {
                            body.write(("<li>" + k + ": " + v + "</li>").getBytes());
                        } catch (IOException e) {
//                                throw new UncheckedIOException(e);
                        }
                    });

                    body.write("</ul></body></html>".getBytes());
                    return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
                } catch (IOException e) {
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            }

            if (Files.isDirectory(requestedPath)) {
                Path indexHtml = requestedPath.resolve("index.html");
                if (Files.exists(indexHtml)) {
                    try {
                        byte[] body = Files.readAllBytes(indexHtml);
                        String mimeType = Files.probeContentType(indexHtml);
                        return new HttpResponse(StatusCode.OK, mimeType, body);
                    } catch (IOException e) {
                        return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    try (Stream<Path> paths = Files.list(requestedPath)) {
                        ByteArrayOutputStream body = new ByteArrayOutputStream();
                        body.write("<html><body><ul>".getBytes());

                        paths.forEach(path -> {
                            try {
                                String name = path.getFileName().toString();
                                String href = req.getPath().endsWith("/") ?
                                        req.getPath() + name :
                                        req.getPath() + "/" + name;
                                body.write(("<li><a href=\"" + href + "\">" + name + "</a></li>").getBytes());
                            } catch (IOException e) {
//                                throw new UncheckedIOException(e);
                            }
                        });

                        body.write("</ul></body></html>".getBytes());
                        return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
                    } catch (IOException e) {
                        return new HttpResponse(StatusCode.NOT_FOUND);
                    }
                }
            }

            if (Files.exists(requestedPath) && !Files.isDirectory(requestedPath)) {
                try {
                    byte[] body = Files.readAllBytes(requestedPath);
                    String mimeType = Files.probeContentType(requestedPath);
                    return new HttpResponse(StatusCode.OK, mimeType, body);
                } catch (IOException e) {
                    return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
                }
            }
        }

        if (req.getMethod() == Methods.POST) {
            if (req.getHeader("content-type").contains("multipart/form-data")) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                ByteArrayOutputStream headers = new ByteArrayOutputStream();
                String contentType = req.getHeader("content-type");
                String boundary = Arrays.stream(contentType.split(";"))
                        .map(String::trim)
                        .filter(s -> s.startsWith("boundary="))
                        .map(s -> s.substring("boundary=".length()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No boundary in content-type"));
                String delimiter = "--" + boundary;
                String closingDelimiter = delimiter + "--";
                String requestBody = req.getBody();
                String[] parts = requestBody.split(Pattern.quote(delimiter));
                List<String> partsList = Arrays.asList(parts);

                try {
                    body.write("<html><body><h2>POST Form</h2><ul>".getBytes());
                    partsList.stream()
                            .skip(1)
                            .filter(p -> !p.equals("--"))
                            .forEach(part -> {
                                String[] section = part.split("\r\n\r\n", 2);
                                if (section.length < 2)
                                    return;
                                String headerBlock = section[0];
                                String[] headerLines = headerBlock.split("\r\n");
                                String name = null;
                                String filename = null;
                                String type = null;

                                for (String line : headerLines) {
                                    if (line.toLowerCase().startsWith("content-disposition")) {
                                        String[] lineParts = line.split(";");
                                        for (String linePart : lineParts) {
                                            linePart = linePart.trim();
                                            if (linePart.startsWith("name=")) {
                                                name = linePart.substring(6, linePart.length() - 1);
                                            } else if (linePart.startsWith("filename=")) {
                                                filename = linePart.substring(10, linePart.length() - 1);
                                            }
                                        }
                                    }
                                    if (line.toLowerCase().startsWith("content-type")) {
                                        type = line.split(": ")[1];
                                    }
                                }
                                String content = section[1].trim();
                                content = content.split("\r\n--")[0];
                                byte[] contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);
                                try {
                                    headers.write(headerBlock.trim().getBytes());
                                    body.write(("<li>file name: " + filename + "</li>").getBytes());
                                    body.write(("<li>content type: " + type + "</li>").getBytes());
                                    body.write(("<li>file size: " + contentBytes.length + "</li>").getBytes());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    body.write("</ul></body></html>".getBytes());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return new HttpResponse(StatusCode.OK, headers.toByteArray(), body.toByteArray());
            }
        }

        if (req.getPath().equals("/guess")) {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            int target = GuessTarget.target;
            System.out.println(target);
            if (req.getMethod() == Methods.GET) {
                try {
                    body.write("<html><body style=\"background-color:red; text-align:center;\"\"\"><h1>Number Guessing Game</h1>".getBytes());
                    body.write("<form method=\"POST\" action=\"/guess\"><label for=\"number\">Input A Number!</label><br>".getBytes());
                    body.write("<input type=\"text\" id=\"number\" name=\"number\"><br>".getBytes());
                    body.write("<input type=\"submit\" value=\"Guess\">".getBytes());
                    body.write(("<p>" + target + "</p>").getBytes());
                    body.write("</body></html>".getBytes());
                    return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
                } catch (IOException e) {
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            } else if (req.getMethod() == Methods.POST) {
                String[] resp = req.getBody().split("=");
                int guess = Integer.parseInt(resp[1]);
                try {
                    body.write("<html><body style=\"background-color:red; text-align:center;\"\"\"><h1>Number Guessing Game</h1>".getBytes());
                    body.write("<form method=\"POST\" action=\"/guess\"><label for=\"number\">Input A Number!</label><br>".getBytes());
                    body.write("<input type=\"text\" id=\"number\" name=\"number\"><br>".getBytes());
                    body.write("<input type=\"submit\" value=\"Guess\">".getBytes());
                    if (guess < target) {
                        body.write("<p>Too Low!</p>".getBytes());
                    } else if (guess > target)
                        body.write("<p>Too High!</p>".getBytes());
                    else {
                        body.write("<p>You got it!!</p>".getBytes());
                        body.write("<button><a href=\"/guess\">Play Again?</a></button>".getBytes());
                        GuessTarget.target = GuessTarget.generateRandom();
                    }
                    body.write(("<p>" + target + "</p>").getBytes());
                    body.write("</body></html>".getBytes());
                    return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
                } catch (IOException e) {
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            }
        }

        return new HttpResponse(StatusCode.NOT_FOUND);
    }

    private HttpResponse handleDirectoryListing(HttpRequest req, String root) {
        Path targetDir = Path.of(root, req.getPath().replaceFirst("/listing", "")).normalize();

        if (Files.isDirectory(targetDir)) {
            try (Stream<Path> paths = Files.list(targetDir)) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                body.write("<html><body><ul>".getBytes());

                paths.forEach(path -> {
                    try {
                        String name = path.getFileName().toString();
                        boolean isDir = Files.isDirectory(path);

                        String href = isDir ?
                                req.getPath() + "/" + name :
                                "/" + name;
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