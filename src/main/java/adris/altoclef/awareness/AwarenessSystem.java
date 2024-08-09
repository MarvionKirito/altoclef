package adris.altoclef.awareness;

//todo
// Implement Rule System
// ignore Butlers
// take current gear into account
// Allow Task to request Threshold Value as a Chat Message Output
public class AwarenessSystem {
    public static int threatLevel;

    public static int getThreatLevel() {
        return threatLevel;
    }

    public static void setThreatLevel(int threatLevel) {
        AwarenessSystem.threatLevel = threatLevel;
    }

    //addThreatLevel
    public static void addThreatLevel(int value) {
        AwarenessSystem.threatLevel += value;
    }
}
