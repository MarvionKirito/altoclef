package adris.altoclef.experimental.AI.Core.RewardSystem;

import java.util.HashMap;

//Quick and Dirty Implementation
public class Reward {


    public static void reward(String[] args) {
        HashMap<String, Integer> currentScores = new HashMap<>();
        currentScores.put("Score", Tracker.score + 1);
    }
}
