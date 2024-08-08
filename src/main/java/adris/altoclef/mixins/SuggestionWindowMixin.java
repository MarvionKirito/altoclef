package adris.altoclef.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.suggestion.Suggestion;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public class SuggestionWindowMixin {
	@Shadow
    @Final
    ChatInputSuggestor field_21615;
    @Shadow
    private int selection;
    @Shadow
    @Final
    private List<Suggestion> suggestions;

    @Inject(method = "complete", at = @At("TAIL"))
    private void overwriteComplete(CallbackInfo ci) {
        ChatInputSuggestorAccessor inputSuggestor = (ChatInputSuggestorAccessor) this.field_21615;
        if (inputSuggestor == null) return;
        TextFieldWidget textFieldWidget = inputSuggestor.getTextField();
        Suggestion suggestion = this.suggestions.get(this.selection);
        int just = suggestion.getRange().getStart() + suggestion.getText().length();
        for (Command command : AltoClef.getCommandExecutor().allCommands()) {
            int justTyped = just - command.getName().length();
            if (command.getName().startsWith(textFieldWidget.getText(), justTyped)) {
                textFieldWidget.eraseCharacters(-command.getName().length() - (1 + command.getName().length()));
                textFieldWidget.setSelectionEnd(textFieldWidget.getCursor());
                textFieldWidget.write("@"+command.getName());
                break;
            }
        }
    }
}
