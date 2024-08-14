package adris.altoclef.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;

@Mixin(ChatInputSuggestor.class)
public interface ChatInputSuggestorAccessor {
	 @Accessor
    TextFieldWidget getTextField();
}
