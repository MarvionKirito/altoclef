package adris.altoclef.experimental.AI.Util.Agent;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

import java.util.Arrays;

import static baritone.api.utils.Helper.mc;

// Thx Envy Client Devs :3
public class MoveUtil {
    static double yaw = mc.player.getYaw();
    private static double speed = 1.0; // Replace with your desired speed value
    static double motionX = -Math.sin(yaw) * speed;
    static double motionY = mc.player.getVelocity().y; // Keep the current Y velocity
    static double motionZ = Math.cos(yaw) * speed;


    public static void setSpeed(double newSpeed) {
        speed = newSpeed;
    }

    public static void strafe() {
        strafe(speed());
    }

    public static double speed() {
        return Math.sqrt(Math.pow(mc.player.getVelocity().getX(), 2) + Math.pow(mc.player.getVelocity().getZ(), 2));
    }

    public static void stop() {
        mc.player.setVelocity(0, mc.player.getVelocity().getY(), 0);
    }


    public static void strafe(double speed) {
        if (!PlayerUtil.isMoving()) {
            return;
        }

        double yaw = getDirection();
        double motionX = -MathHelper.sin((float) yaw) * speed; // Inverted direction
        double motionZ = MathHelper.cos((float) yaw) * speed; // Inverted direction
        mc.player.setVelocity(new Vec3d(motionX, mc.player.getVelocity().getY(), motionZ));
    }


    public void strafe(double speed, float yaw) {
        if (!PlayerUtil.isMoving()) {
            return;
        }

        yaw = (float) getDirection();
        double motionX = MathHelper.sin(yaw) * speed; // Forward movement
        double motionZ = -MathHelper.cos(yaw) * speed; // Right movement
        mc.player.setVelocity(new Vec3d(motionX, mc.player.getVelocity().getY(), motionZ));
    }

    public static double getDirection() {
        double rotationYaw = MinecraftClient.getInstance().player.getYaw();
        if (MinecraftClient.getInstance().player.input.movementForward < 0) {
            rotationYaw += 180;
        }
        double forward = 1;
        if (MinecraftClient.getInstance().player.input.movementForward < 0) {
            forward = -0.5;
        } else if (MinecraftClient.getInstance().player.input.movementForward > 0) {
            forward = 0.5;
        }
        if (MinecraftClient.getInstance().player.input.movementSideways > 0) {
            rotationYaw -= 90 * forward;
        }
        if (MinecraftClient.getInstance().player.input.movementSideways < 0) {
            rotationYaw += 90 * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static double predictedMotion(double motion) {
        return (motion - 0.08) * 0.98;
    }


    public static double predictedMotion(double motion, int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;

        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - 0.08) * 0.98;
        }

        return predicted;
    }

    public static double getSpeed() {
        return Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
    }

    public static double getJumpMotion(float motionY) {

        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            final int amplifier = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            motionY += (amplifier + 1) * 0.1F;
        }

        return motionY;
    }

    public static double getPredictedMotionY(final double motionY) {
        return (motionY - 0.08) * 0.98F;
    }

    public static double getAllowedHorizontalDistance() {
        double horizontalDistance;
        boolean useBaseModifiers = false;

        double MOD_WEB = 1.0; // Replace 1.0 with your desired modifier for web
        double WALK_SPEED = 0.1; // Replace 0.1 with your desired walk speed
        double MOD_SWIM = 0.2;
        double MOD_SNEAK = 0.3;
        double MOD_SPRINTING = 1.3;

        int playerX = (int) Math.floor(mc.player.getX());
        int playerY = (int) Math.floor(mc.player.getY() - 0.2); // Adjust the Y value as needed
        int playerZ = (int) Math.floor(mc.player.getZ());

        Block blockBelowPlayer = mc.world.getBlockState(new BlockPos(playerX, playerY, playerZ)).getBlock();
        Block blockAtPlayerFeet = mc.world.getBlockState(new BlockPos(playerX, playerY, playerZ)).getBlock();


        if (blockBelowPlayer == Blocks.COBWEB) {
            horizontalDistance = MOD_WEB * WALK_SPEED;
        } else if (blockAtPlayerFeet == Blocks.WATER || blockAtPlayerFeet == Blocks.LAVA) {
            // Player is in water or lava
            // Your code here
            horizontalDistance = MOD_SWIM * WALK_SPEED;
            //todo
            // Implement the DepthStrider Code
/*            final int depthStriderLevel = depthStriderLevel();
            if (depthStriderLevel > 0) {
                horizontalDistance *= MOD_DEPTH_STRIDER[depthStriderLevel];
                useBaseModifiers = true;
            }*/

        } else if (mc.player.isSneaking()) {
            horizontalDistance = MOD_SNEAK * WALK_SPEED;
        } else {
            horizontalDistance = WALK_SPEED;
            useBaseModifiers = true;
        }

        if (useBaseModifiers) {
            if (mc.player.isSprinting()) {
                horizontalDistance *= MOD_SPRINTING;
            }

            if (mc.player.hasStatusEffect(StatusEffects.SPEED) && mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() > 0) {
                // Adjust the speed based on the potion effect
                double speedMultiplier = 1.0 + (0.2 * (mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1));
                horizontalDistance *= speedMultiplier;
            }

            // Check for the slowness potion effect
            if ((mc.player.hasStatusEffect(StatusEffects.SLOWNESS))) {
                // Set a specific horizontal distance when affected by slowness
                horizontalDistance = 0.29;
            }
        }

        return horizontalDistance;
    }

    public static double[] moveFlying(float strafe, float forward, final boolean onGround, final float yaw, final boolean sprinting) {
        float friction = 0.02f;
        final float playerWalkSpeed = mc.player.getMovementSpeed();

        if (onGround) {
            final float f4 = 0.6f * 0.91f;
            final float f = 0.16277136F / (f4 * f4 * f4);
            friction = playerWalkSpeed / 2.0f * f;
        }

        if (sprinting) {
            friction = (float) ((double) friction + ((onGround) ? (playerWalkSpeed / 2.0f) : 0.02f) * 0.3D);
        }

        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;

            final float f1 = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            final float f2 = MathHelper.cos(yaw * (float) Math.PI / 180.0F);

            final double motionX = (strafe * f2 - forward * f1);
            final double motionZ = (forward * f2 + strafe * f1);

            return new double[]{motionX, motionZ};
        }

        return null;
    }

    public Vector2d moveFlyingVec(float strafe, float forward, final boolean onGround, final float yaw, final boolean sprinting) {
        double[] values = moveFlying(strafe, forward, onGround, yaw, sprinting);
        if (values == null) return null;
        return new Vector2d(values[0], values[1]);
    }

    public Double moveFlyingSpeed(float strafe, float forward, final boolean onGround, final float yaw, final boolean sprinting) {
        double[] speed = moveFlying(strafe, forward, onGround, yaw, sprinting);

        if (speed == null) return null;

        return Math.hypot(speed[0], speed[1]);
    }

    public Double moveFlyingSpeed(final boolean sprinting) {
        double[] speed = moveFlying(0.98f, 0.98f, mc.player.isOnGround(), 180, sprinting);

        if (speed == null) return null;

        return Math.hypot(speed[0], speed[1]);
    }

    public static void moveFlying(double increase) {
        double motionX = mc.player.getVelocity().x;
        double motionZ = mc.player.getVelocity().z;
        if (!PlayerUtil.isMoving()) return;
        final double yaw = MoveUtil.getDirection();
        motionX += -MathHelper.sin((float) yaw) * increase;
        motionZ += MathHelper.cos((float) yaw) * increase;
        mc.player.setVelocity(motionX, mc.player.getVelocity().y, motionZ);
    }


    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static void useDiagonalSpeed() {
        KeyBinding[] gameSettings = new KeyBinding[]{mc.options.forwardKey, mc.options.rightKey, mc.options.backKey, mc.options.leftKey};

        final int[] down = {0};

        Arrays.stream(gameSettings).forEach(keyBinding -> {
            down[0] = down[0] + (keyBinding.isPressed() ? 1 : 0);
        });

        boolean active = down[0] == 1;

        if (!active) return;

        final double groundIncrease = (0.1299999676734952 - 0.12739998266255503) + 1E-7 - 1E-8;
        final double airIncrease = (0.025999999334873708 - 0.025479999685988748) - 1E-8;
        final double increase = mc.player.isOnGround() ? groundIncrease : airIncrease;

        moveFlying(increase);
    }
}