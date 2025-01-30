package adris.altoclef.cheatmenu.UI.screens.clickgui.setting;


import adris.altoclef.cheatmenu.UI.screens.clickgui.ModuleButton;
import adris.altoclef.cheatmenu.modules.settings.ModeSetting;
import adris.altoclef.cheatmenu.modules.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class ModeBox extends Component {

    protected MinecraftClient mc = MinecraftClient.getInstance();
    private final ModeSetting modeSet;

    public ModeBox(Setting setting, ModuleButton parent, int offset) {
        super(setting, parent, offset);
        this.modeSet = (ModeSetting) setting;
    }


    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        matrices.fill(parent.parent.x, parent.parent.y + parent.offset + offset, parent.parent.x + parent.parent.width, parent.parent.y + parent.offset + offset + parent.parent.height, new Color(0, 0, 0, 160).getRGB());
        int textOffset = ((parent.parent.height /2) - mc.textRenderer.fontHeight / 2);
        matrices.drawText(mc.textRenderer, modeSet.getName() + ": " + modeSet.getMode(), parent.parent.x + textOffset, parent.parent.y + parent.offset + offset + textOffset, -1 , true);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            modeSet.cycle();
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
}
