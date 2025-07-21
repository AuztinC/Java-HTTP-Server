package Server;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientHandler {
    GuessTarget guessState = GuessTarget.getInstance();

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
                List<String> pathString = List.of(req.getPath().substring(6).split("&"));
                Map<String, String> queryParams = pathString.stream()
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

            if (req.getPath().contains("/ping")) {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                int waitTime = 0;
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                if (req.getPath().contains("/ping/")) {
                    waitTime = Integer.parseInt(req.getPath().substring(6));
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                LocalDateTime later = now.plusSeconds(waitTime);
                String formattedNow = dateFormat.format(now);
                String formattedLater = dateFormat.format(later);
                try {
                    body.write("<html><body><h2>Ping</h2><ul>".getBytes());
                    body.write(("<li>start time: " + formattedNow + "</li>").getBytes());
                    body.write(("<li>end time: " + formattedLater + "</li>").getBytes());
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
                                String filename = null;
                                String type = null;

                                for (String line : headerLines) {
                                    if (line.toLowerCase().startsWith("content-disposition")) {
                                        String[] lineParts = line.split(";");
                                        for (String linePart : lineParts) {
                                            linePart = linePart.trim();
                                            if (linePart.startsWith("filename=")) {
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
            ByteArrayOutputStream header = new ByteArrayOutputStream();
            String userId = extractUserId(req.getHeader("cookie"));
            int target = guessState.getOrCreateTarget(userId);
            int count = guessState.getGuessCount(userId);
            if (req.getMethod() == Methods.GET) {
                try {
                    if (userId == null || userId.isEmpty()) {
                        UUID newId = UUID.randomUUID();
                        userId = newId.toString();
                        header.write(("Set-Cookie: userId=" + userId + "; Path=/\r\n").getBytes());
                        header.write("Content-Type: text/html".getBytes());
                    }
                    guessLandingPage(body, count);
                    body.write(("<p>" + target + "</p>").getBytes());
                    body.write("</body></html>".getBytes());
                    return new HttpResponse(StatusCode.OK, header.toByteArray(), body.toByteArray());
                } catch (IOException e) {
                    return new HttpResponse(StatusCode.NOT_FOUND);
                }
            } else if (req.getMethod() == Methods.POST) {
                guessState.decrementGuessCount(userId);
                count = guessState.getGuessCount(userId);
                String[] resp = req.getBody().split("=");
                try {
                    int guess = Integer.parseInt(resp[1]);
                    guessLandingPage(body, count);
                    handleGuess(guess, target, body, userId);
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

    private void handleGuess(int guess, int target, ByteArrayOutputStream body, String userId) throws IOException {
        if (guess < target) {
            body.write("<p>Too Low!</p>".getBytes());
        } else if (guess > target)
            body.write("<p>Too High!</p>".getBytes());
        else {
            body.write("<p>You got it!!</p>".getBytes());
            playAgainButton(body);
            guessState.resetUser(userId);
        }
        if (guessState.getGuessCount(userId) == 0) {
            body.write("<p>Oops! Better luck next time.</p><br>".getBytes());
            playAgainButton(body);
            guessState.resetUser(userId);
        }
    }

    private String extractUserId(String cookieHeader) {
        if (cookieHeader == null) return null;
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            cookie = cookie.trim();
            if (cookie.startsWith("userId=")) {
                return cookie.substring("userId=".length());
            }
        }
        return null;
    }

    private static void guessLandingPage(ByteArrayOutputStream body, int count) throws IOException {
        body.write("<html><body style=\"background-color:red; text-align:center;\"><h1>Number Guessing Game</h1>".getBytes());
        body.write(("<h3>You currently have " + count + " left").getBytes());
        body.write("<form method=\"POST\" action=\"/guess\"><label for=\"number\">Input A Number!</label><br>".getBytes());
        body.write("<input type=\"number\" id=\"number\" name=\"number\" autofocus required step=\"1\"><br>".getBytes());
        body.write("<input type=\"submit\" value=\"Guess\">".getBytes());
    }

    private static void playAgainButton(ByteArrayOutputStream body) throws IOException {
        body.write(("<a href=\"/guess\" style=\"  " +
                "background-color: black;\n" +
                "color: white;\n" +
                "padding: 14px 25px;\n" +
                "text-align: center;\n" +
                "text-decoration: none;\n" +
                "display: inline-block;\">Play Again?</a>").getBytes());
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