package adris.altoclef;

import adris.altoclef.eventbus.events.ClientTickEvent;
import adris.altoclef.ui.CommandStatusOverlayGUI;
import adris.altoclef.ui.CommandStatusOverlayScreen;
import baritone.api.event.events.TickEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static final String CATEGORY = "key.categories.altoclef";

    private static AltoClef mod;

    public KeyBindings(AltoClef mod) {
        KeyBindings.mod = mod;
    }

    public void registerBindings() {
        registerOpenGUIKey();
    }

    private void registerOpenGUIKey() {
        KeyBinding openGUIkey = new KeyBinding("key.altoclef.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
        KeyBindingHelper.registerKeyBinding(openGUIkey);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGUIkey.wasPressed()) {
                // TODO: Open GUI
            }
        });
    }
}