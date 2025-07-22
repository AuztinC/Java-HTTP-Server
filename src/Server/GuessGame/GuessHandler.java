package Server.GuessGame;

import Server.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class GuessHandler implements RouteHandler{
    private final GuessTarget guessState = GuessTarget.getInstance();

    public HttpResponse handle(HttpRequest req) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        String userId = extractUserId(req.getHeader("cookie"));

        if (userId == null || userId.isEmpty()) {
            userId = UUID.randomUUID().toString();
            try {
                header.write(("Set-Cookie: userId=" + userId + "; Path=/\r\n").getBytes());
                header.write("Content-Type: text/html".getBytes());
            } catch (IOException e) {
                return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
            }
        }

        int target = guessState.getOrCreateTarget(userId);
        int count = guessState.getGuessCount(userId);

        try {
            if (req.getMethod() == Methods.GET) {
                guessLandingPage(body, count);
                body.write(("<p>" + target + "</p>").getBytes());
                body.write("</body></html>".getBytes());
                return new HttpResponse(StatusCode.OK, header.toByteArray(), body.toByteArray());

            } else if (req.getMethod() == Methods.POST) {
                guessState.decrementGuessCount(userId);
                count = guessState.getGuessCount(userId);
                String[] resp = req.getBody().split("=");
                int guess = Integer.parseInt(resp[1]);
                guessLandingPage(body, count);
                handleGuess(guess, target, body, userId);
                body.write(("<p>" + target + "</p>").getBytes());
                body.write("</body></html>".getBytes());
                return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
            }
        } catch (IOException | NumberFormatException e) {
            return new HttpResponse(StatusCode.NOT_FOUND);
        }

        return new HttpResponse(StatusCode.NOT_FOUND);
    }

    private void handleGuess(int guess, int target, ByteArrayOutputStream body, String userId) throws IOException {
        if (guess < target) {
            body.write("<p>Too Low!</p>".getBytes());
        } else if (guess > target) {
            body.write("<p>Too High!</p>".getBytes());
        } else {
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
}
