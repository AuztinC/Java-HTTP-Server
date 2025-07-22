package Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PingHandler implements RouteHandler{
    Sleep sleeper;

    public PingHandler(Sleep sleep){
        this.sleeper = sleep;
    }

    public HttpResponse handle(HttpRequest req) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        int waitTime = 0;
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        if (req.getPath().contains("/ping/")) {
            waitTime = Integer.parseInt(req.getPath().substring(6)) * 1000;
            try {
                sleeper.sleep(waitTime);
            } catch (InterruptedException e) {
                return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
            }
        }
        LocalDateTime later = LocalDateTime.now();
        String formattedNow = dateFormat.format(now);
        String formattedLater = dateFormat.format(later);
        try {
            body.write("<html><body><h2>Ping</h2><ul>".getBytes());
            body.write(("<li>start time: " + formattedNow + "</li>").getBytes());
            body.write(("<li>end time: " + formattedLater + "</li>").getBytes());
            body.write("</ul></body></html>".getBytes());
            return new HttpResponse(StatusCode.OK, "text/html", body.toByteArray());
        } catch (IOException e) {
            return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }
}
