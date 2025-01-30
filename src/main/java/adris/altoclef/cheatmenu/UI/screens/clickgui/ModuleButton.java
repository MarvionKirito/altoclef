package adris.altoclef.cheatmenu.UI.screens.clickgui;

import adris.altoclef.cheatmenu.Mod;
import adris.altoclef.cheatmenu.UI.screens.clickgui.setting.CheckBox;
import adris.altoclef.cheatmenu.UI.screens.clickgui.setting.ModeBox;
import adris.altoclef.cheatmenu.UI.screens.clickgui.setting.Slider;
import adris.altoclef.cheatmenu.modules.settings.BooleanSetting;
import adris.altoclef.cheatmenu.modules.settings.ModeSetting;
import adris.altoclef.cheatmenu.modules.settings.NumberSetting;
import adris.altoclef.cheatmenu.modules.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import adris.altoclef.cheatmenu.UI.screens.clickgui.setting.Component;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton {

    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public Mod module;
    public Frame parent;
    public int offset;
    public List<Component> components;
    public boolean extended;

    public ModuleButton(Mod module, Frame parent, int offset) {
        this.module = module;
        this.parent = parent;
        this.offset = offset;
        this.components = new ArrayList<>();
        this.extended = false;

        int setOffset = parent.height;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) {
                components.add(new CheckBox(setting, this, setOffset));
            } else if (setting instanceof ModeSetting) {
                components.add(new ModeBox(setting, this, setOffset));
            } else if (setting instanceof NumberSetting) {
                components.add(new Slider(setting, this, setOffset));
            }
            setOffset += parent.height;
        }
    }

    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        matrices.fill(parent.x, parent.y + offset, parent.x + parent.width, parent.y + offset + parent.height, new Color(0, 0, 0, 160).getRGB());
        if (isHovered(mouseX, mouseY)) matrices.fill(parent.x, parent.y + offset, parent.x + parent.width, parent.y + offset + parent.height, new Color(0, 0, 0, 160).getRGB());
        matrices.drawText(mc.textRenderer, module.getName(), parent.x + 2, parent.y + offset + 2, module.isEnabled() ? Color.RED.getRGB() : Color.WHITE.getRGB(), true);

        if (extended) {
            for (Component component : components) {
                component.render(matrices, mouseX, mouseY, delta);
            }
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                module.toggle();
            }
            else if (button == 1) {
                extended = !extended;
                parent.updateButtons();

            }
        }

        for (Component component : components) {
            component.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (Component component : components) {
            component.mouseReleased(mouseX, mouseY, button);
        }
    }
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > parent.x && mouseX < parent.x + parent.width && mouseY > parent.y + offset && mouseY < parent.y + offset + parent.height;
    }
}
