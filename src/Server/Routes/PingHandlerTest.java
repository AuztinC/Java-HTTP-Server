package Server.Routes;

import Server.HTTP.HttpRequest;
import Server.Methods;
import Server.Sleep;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PingHandlerTest {

    private static long millis;

    private class FakeSleeper implements Sleep {

        @Override
        public void sleep(int time) throws InterruptedException {
            PingHandlerTest.millis = time;
        }
    }

    @Test
    public void sleepsForOneSecond() {
        PingHandler handler = new PingHandler(new FakeSleeper());
        HttpRequest req = new HttpRequest(Methods.GET, "/ping/1");
        handler.handle(req);
        assertEquals(1000, millis);
    }
}
