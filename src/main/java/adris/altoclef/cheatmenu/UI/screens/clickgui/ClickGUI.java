package adris.altoclef.cheatmenu.UI.screens.clickgui;

import adris.altoclef.cheatmenu.Mod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;
import java.awt.image.renderable.RenderContext;
import java.util.ArrayList;
import java.util.List;

import static adris.altoclef.cheatmenu.UI.screens.clickgui.ModuleButton.mc;

public class ClickGUI extends Screen {

    private final List<Frame> frames;

    public ClickGUI() {
        super(Text.literal("ClickGUI"));

        frames = new ArrayList<>();
        int x = 20;

        Frame combatFrame = new Frame(Mod.Category.COMBAT, x, 30, 100, 15);
        frames.add(combatFrame);
        x += 120;
        Frame settingsFrame = new Frame(Mod.Category.MISC, x, 30, 100, 15);
        frames.add(settingsFrame);
        x += 120;
        Frame playerFrame = new Frame(Mod.Category.PLAYER, x, 30, 100, 15);
        frames.add(playerFrame);
        x += 120;
        Frame renderFrame = new Frame(Mod.Category.RENDER, x, 30, 100, 15);
        frames.add(renderFrame);
        x += 120;
        Frame movementFrame = new Frame(Mod.Category.MOVEMENT, x, 30, 100, 15);
        frames.add(movementFrame);
        x += 120;
        Frame worldFrame = new Frame(Mod.Category.WORLD, x, 30, 100, 15);
        frames.add(worldFrame);
        x += 120;
        Frame chatFrame = new Frame(Mod.Category.EXPLOIT, x, 30, 100, 15);
        frames.add(chatFrame);
        x += 120;
        Frame hudFrame = new Frame(Mod.Category.DEVELOPMENT, x, 30, 100, 15);
        frames.add(hudFrame);
        // Add the frames to the list of frames
/*        frames.add(settingsFrame);
        frames.add(playerFrame);
        frames.add(renderFrame);
        frames.add(movementFrame);
        frames.add(worldFrame);
        frames.add(chatFrame);
        frames.add(hudFrame);*/

/*        frames = new ArrayList<>();

        for (Mod.Category category : Mod.Category.values()) {
            frames.add(new Frame(category, x, 30, 100, 15,category.name()));
            x += 120;
        }*/
    }

    public static ClickGUI INSTANCE = new ClickGUI();


    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        matrices.fill(0, 0, 100000000, 100000000, 0x80000000);
        //Draw a text that says "Envy on Top" in the bottom right corner
        matrices.drawCenteredTextWithShadow(this.textRenderer, "Bleed Client", this.width - 50, this.height - 20, Color.RED.getRGB());
        for (Frame frame : frames) {
            frame.render(matrices, mouseX, mouseY, delta);
            frame.updatePosition(mouseX, mouseY);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public static void open() {
        mc.setScreen(INSTANCE);
    }
}
