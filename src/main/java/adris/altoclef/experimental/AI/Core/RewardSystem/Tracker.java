package adris.altoclef.experimental.AI.Core.RewardSystem;

import java.util.HashMap;

//Quick and Dirty Implementation
public class Tracker {

    public static int score = 0;

    public static void currentScores(String[] args) {
        HashMap<String, Integer> currentScores = new HashMap<>();
        currentScores.put("Score", 0);
    }
}
