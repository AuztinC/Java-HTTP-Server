package Server;

public class ThreadSleep implements Sleep{
    @Override
    public void sleep(int time) throws InterruptedException {
        Thread.sleep(time);
    }
}
