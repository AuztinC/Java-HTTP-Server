package Server;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GuessTargetTest {

    @Test
    public void testGetOrCreateTargetReturnsSameNumberForSameUser() {
        GuessTarget game = GuessTarget.getInstance();

        String userId = "user1";
        int target1 = game.getOrCreateTarget(userId);
        int target2 = game.getOrCreateTarget(userId);

        assertEquals(target1, target2, "Target number should be consistent for the same user.");
    }

    @Test
    public void testDifferentUsersLikelyGetDifferentTargets() {
        GuessTarget game = GuessTarget.getInstance();

        String userA = "userA";
        String userB = "userB";

        int targetA = game.getOrCreateTarget(userA);
        int targetB = game.getOrCreateTarget(userB);

        // 1% chance of match in a 1–100 range
        assertNotEquals(targetA, targetB, "Different users should usually get different target numbers.");
    }

    @Test
    public void testResetUserCreatesNewTarget() {
        GuessTarget game = GuessTarget.getInstance();

        String userId = "user1";
        int target1 = game.getOrCreateTarget(userId);
        game.resetUser(userId);
        int target2 = game.getOrCreateTarget(userId);

        assertNotEquals(target1, target2, "User should get a new target after reset.");
    }
}
