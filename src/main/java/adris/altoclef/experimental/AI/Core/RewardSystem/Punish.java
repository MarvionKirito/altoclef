package adris.altoclef.experimental.AI.Core.RewardSystem;

import java.util.HashMap;

//Quick and Dirty Implementation
public class Punish {

    public static void punish(String[] args) {
        HashMap<String, Integer> currentScores = new HashMap<>();
        currentScores.put("Score", Tracker.score - 1);
    }
}
