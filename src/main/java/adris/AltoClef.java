package adris.altoclef;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientChatCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleBot implements ModInitializer {
    private MinecraftClient mc;
    private boolean isRunning;

    @Override
    public void onInitialize() {
        mc = MinecraftClient.getInstance();
        isRunning = false;

        ClientChatCallback.EVENT.register((client, message) -> {
            if (message.equalsIgnoreCase("&run")) {
                isRunning = !isRunning;
                client.player.sendMessage(Text.of("Bot is now " + (isRunning ? "running" : "stopped")), false);
                return true; // ป้องกันไม่ให้ข้อความแสดงในแชท
            }
            return false;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isRunning && client.player != null) {
                // หา entity ที่ใกล้ที่สุด
                List<Entity> entities = mc.world.getEntities().stream()
                        .filter(e -> e instanceof LivingEntity && e != client.player)
                        .collect(Collectors.toList());

                if (!entities.isEmpty()) {
                    Entity nearestEntity = entities.get(0);
                    double nearestDistance = Double.MAX_VALUE;

                    for (Entity entity : entities) {
                        double distance = client.player.squaredDistanceTo(entity);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestEntity = entity;
                        }
                    }

                    // เดินไปหา entity ที่ใกล้ที่สุด
                    Vec3d targetPos = nearestEntity.getPos();
                    client.player.getNavigation().startMovingTo(targetPos.x, targetPos.y, targetPos.z, 1.0);

                    // โจมตี entity เมื่ออยู่ใกล้พอ
                    if (nearestDistance < 4) {
                        client.interactionManager.attackEntity(client.player, nearestEntity);
                        client.player.swingHand(client.player.getActiveHand());
                    }
                } else {
                    // หยุดการเคลื่อนไหวเมื่อไม่มี entity อยู่ใกล้
                    client.player.setVelocity(0, 0, 0);
                }
            }
        });
    }
}
