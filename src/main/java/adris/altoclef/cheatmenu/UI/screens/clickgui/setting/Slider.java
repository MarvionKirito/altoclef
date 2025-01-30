package adris.altoclef.cheatmenu.UI.screens.clickgui.setting;

import adris.altoclef.cheatmenu.UI.screens.clickgui.ModuleButton;
import adris.altoclef.cheatmenu.modules.settings.NumberSetting;
import adris.altoclef.cheatmenu.modules.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Slider extends Component{
    protected MinecraftClient mc = MinecraftClient.getInstance();

    public NumberSetting numSet;

    private boolean sliding = false;

    public Slider(Setting setting, ModuleButton parent, int offset) {
        super(setting, parent, offset);
        this.numSet = (NumberSetting) setting;
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        int renderWidth = (int) (parent.parent.width * (numSet.getValue() - numSet.getMin()) / (numSet.getMax() - numSet.getMin()));
        matrices.fill(parent.parent.x, parent.parent.y + parent.offset + offset, parent.parent.x + parent.parent.width, parent.parent.y + parent.offset + offset + parent.parent.height, new Color(0, 0, 0, 160).getRGB());

        double diff = Math.min(parent.parent.width, Math.max(0, mouseX - parent.parent.x));
        matrices.fill(parent.parent.x, parent.parent.y + parent.offset + offset, parent.parent.x + renderWidth, parent.parent.y + parent.offset + offset + parent.parent.height, new Color(255, 0, 0, 160).getRGB());

        if (sliding) {
            if (diff == 0) {
                numSet.setValue(numSet.getMin());
            } else {
                numSet.setValue(roundToPlace((diff / parent.parent.width) * (numSet.getMax() - numSet.getMin()) + numSet.getMin(), 2));
            }
        }
        int textOffset = ((parent.parent.height / 2) - mc.textRenderer.fontHeight / 2);
        matrices.drawText(mc.textRenderer, numSet.getName() + ": " + roundToPlace(numSet.getValue(), 2), parent.parent.x + textOffset, parent.parent.y + parent.offset + offset + textOffset, -1, true);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) sliding = true;
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        sliding = false;
        super.mouseReleased(mouseX, mouseY, button);
    }

    private double roundToPlace(double value, int place) {
        if (place <0) {
            return value;
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(place, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
