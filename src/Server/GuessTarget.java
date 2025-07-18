package Server;

public class GuessTarget {
    public static int target = generateRandom();
    public static int count = 0;

    public static int generateRandom() {
        return (int)(Math.random() * 100) + 1;
    }
}
