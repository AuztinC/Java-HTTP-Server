package Server.GuessGame;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GuessTarget {
    private static final GuessTarget INSTANCE = new GuessTarget();
    private final Map<String, Integer> userTargets = new HashMap<>();
    private final Map<String, Integer> userGuesses = new HashMap<>();
    private final Random random = new Random();

    private GuessTarget() {}

    public int getGuessCount(String userId) {
        return userGuesses.getOrDefault(userId, 7);
    }

    public void decrementGuessCount(String userId) {
        userGuesses.put(userId, getGuessCount(userId) - 1);
    }

    public static GuessTarget getInstance() {
        return INSTANCE;
    }

    public int getOrCreateTarget(String userId) {
        return userTargets.computeIfAbsent(userId, id -> random.nextInt(100) + 1);
    }

    public boolean checkGuess(String userId, int guess) {
        return guess == getOrCreateTarget(userId);
    }

    public void resetUser(String userId) {
        userTargets.remove(userId);
        userGuesses.remove(userId);
    }
    public void setTarget(String userId, int target, int guessCount) {
        userTargets.put(userId, target);
        userGuesses.put(userId, guessCount);
    }

}

