package adris.altoclef.cheatmenu.UI.screens.clickgui;

import adris.altoclef.cheatmenu.Mod;
import adris.altoclef.cheatmenu.UI.screens.clickgui.setting.Component;
import adris.altoclef.cheatmenu.managers.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Frame {

    public int x, y, width, height, dragX, dragY;
    public Mod.Category category;
    public boolean dragging, extended;

    private final List<ModuleButton> buttons;

    protected MinecraftClient mc = MinecraftClient.getInstance();
    public Frame(Mod.Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.dragging = false;
        this.extended = false;



        buttons = new ArrayList<>();


        int offset = height;
        for (Mod mod : ModuleManager.INSTANCE.getModulesInCategory(category)) {
            buttons.add(new ModuleButton(mod, this, offset));
            offset += height;
        }
    }


    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        matrices.fill(x, y, x + width, y + height, Color.red.getRGB());
        //draw a black outline
        matrices.fill(x, y, x + width, y + 1, Color.black.getRGB());
        matrices.fill(x, y, x + 1, y + height, Color.black.getRGB());
        matrices.fill(x + width - 1, y, x + width, y + height, Color.black.getRGB());
        matrices.fill(x, y + height - 1, x + width, y + height, Color.black.getRGB());
        matrices.drawTextWithShadow(mc.textRenderer,category.name, x + 2, y + 2, -1);
        matrices.drawTextWithShadow(mc.textRenderer,extended ? "-" : "+", x + width - 2 - mc.textRenderer.getWidth("+"), y + 2, -1);

        if (extended) {
            for (ModuleButton button : buttons) {
                button.render(matrices, mouseX, mouseY, delta);
            }
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            }
            else if (button == 1) {
                extended = !extended;
                updateButtons();
            }
        }
        if (extended) {
            for (ModuleButton mb : buttons) {
                mb.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) dragging = false;

        for (ModuleButton mb : buttons) {
            mb.mouseReleased(mouseX, mouseY, button);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }


    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int)mouseX - dragX;
            y = (int)mouseY - dragY;
        }
    }

    public void updateButtons() {
        int offset = height;

        for (ModuleButton button : buttons) {
            button.offset = offset;
            offset += height;

            if (button.extended) {
                for (Component component : button.components) {
                    if (component.setting.isVisible()) offset += height;
                }
            }
        }
    }

}
