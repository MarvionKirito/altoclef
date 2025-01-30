package adris.altoclef.experimental.AI.Util.Agent;

import adris.altoclef.util.Dimension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import static baritone.api.utils.Helper.mc;

public class PlayerUtil {
    public static double lastHealth = -0.1;
    public static boolean healthChanged;
    public static boolean isDead;

    public static void trackAgentHealth() {
        mc.player.getHealth();
        if (lastHealth == -0.1) lastHealth = mc.player.getHealth();
        else if (mc.player.getHealth() != lastHealth) {
            healthChanged = true;
            lastHealth = mc.player.getHealth();
        }
        else healthChanged = false;
    }

    public static void isDead() {
        if (mc.player.getHealth() == 0) isDead = true;
        else isDead = false;
    }

    public static boolean isMoving() {
        return mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
    }

    public static boolean isSprinting() {
        return mc.player.isSprinting() && (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0);
    }

    public static Dimension getDimension() {
        if (mc.world == null) return Dimension.OVERWORLD;

        return switch (mc.world.getRegistryKey().getValue().getPath()) {
            case "the_nether" -> Dimension.NETHER;
            case "the_end" -> Dimension.END;
            default -> Dimension.OVERWORLD;
        };
    }

    ///////////////////////////// // *Crystal PvP Related* // ///////////////////////////////////////////////
    public static boolean isInHole(PlayerEntity p) {
        BlockPos pos = p.getBlockPos();
        return !mc.world.getBlockState(pos.add(1, 0, 0)).isAir() && !mc.world.getBlockState(pos.add(-1, 0, 0)).isAir() && !mc.world.getBlockState(pos.add(0, 0, 1)).isAir() && !mc.world.getBlockState(pos.add(0, 0, -1)).isAir() && !mc.world.getBlockState(pos.add(0, -1, 0)).isAir();
    }

    public static boolean isSurrounded(PlayerEntity target) {
        return !mc.world.getBlockState(target.getBlockPos().add(1, 0, 0)).isAir() && !mc.world.getBlockState(target.getBlockPos().add(-1, 0, 0)).isAir() && !mc.world.getBlockState(target.getBlockPos().add(0, 0, 1)).isAir() && !mc.world.getBlockState(target.getBlockPos().add(0, 0, -1)).isAir();
    }

    public static boolean isBurrowed(PlayerEntity target) {
        return !mc.world.getBlockState(target.getBlockPos()).isAir();
    }
    /////////////////////////////////////////////////////////////////////////////////////////
}
