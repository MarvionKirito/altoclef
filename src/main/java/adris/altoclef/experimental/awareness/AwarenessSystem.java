package adris.altoclef.experimental.awareness;


import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import net.minecraft.entity.passive.VillagerEntity;

// might be removed if AI system is finished
// might implement this as a Dynamic Combat system instead
public class AwarenessSystem {
    //Threat level
    public static int threatLevel;

    public static int getThreatLevel() {
        return threatLevel;
    }

    public static void setThreatLevel(int threatLevel) {
        AwarenessSystem.threatLevel = threatLevel;
    }
    public static void addThreatLevel(int value) {
        AwarenessSystem.threatLevel += value;
    }

    // Potential Village
    public static boolean nearVillage;
    public static boolean isNearVillage() {
        return nearVillage;
    }
    public static void setNearVillage(boolean nearVillage) {
        AwarenessSystem.nearVillage = nearVillage;
    }
    public static boolean isNearVillager(AltoClef mod) {
        //check if any villages are near
        if (mod.getEntityTracker().entityFound(VillagerEntity.class)) {
            nearVillage = true;
            Debug.logMessage("Villager found! || Potential Village");
        }
        return nearVillage;
    }
}
