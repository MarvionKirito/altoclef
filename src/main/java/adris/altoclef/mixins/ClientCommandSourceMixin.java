package adris.altoclef.mixins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import adris.altoclef.Debug;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.ChatSuggestionsS2CPacket;

@Mixin(ClientCommandSource.class)
public class ClientCommandSourceMixin {
	private String currentInput;
	private static final char[] SPECIAL_CUTOFF_CHARS = new char[]{'[', '=', ','};

	/**
	 * In order to fetch all possible suggestions the last argument needs to be empty.
	 */
	@Redirect(method = "getCompletions", at = @At(target = "Lcom/mojang/brigadier/context/CommandContext;getInput()Ljava/lang/String;", value = "INVOKE"))
	private String processInput(CommandContext<?> context) {
		String input = context.getInput();
		
		int cutoffIndex = input.lastIndexOf(' ');
		
		// special cutoff chars are added to fix #6
		for (char c : SPECIAL_CUTOFF_CHARS) {
			int index = input.lastIndexOf(c);
			if (index > cutoffIndex)
				cutoffIndex = index;
		}
		currentInput = input.substring(cutoffIndex + 1);
		return input.substring(0, cutoffIndex + 1);
	}
	
//	@Overwrite
//	public Collection<String> getChatSuggestions() {
//		List<String> suggestions = new ArrayList<String>();
//		suggestions.add("addfe");
//		return suggestions;
//	}
	
	@ModifyVariable(method = "onCommandSuggestions", at = @At(target = "Ljava/util/concurrent/CompletableFuture;complete(Ljava/lang/Object;)Z", value = "INVOKE", shift = At.Shift.BEFORE), index = 2, argsOnly = true)
	private Suggestions filterSuggestions(Suggestions suggestions) {
		int start = Integer.MAX_VALUE;
		int end = Integer.MIN_VALUE;
		List<Suggestion> suggestionList = new ArrayList<>();
		for (Suggestion suggestion : suggestions.getList()) {
			if (CommandSource.shouldSuggest(currentInput.toLowerCase(Locale.ROOT), suggestion.getText().toLowerCase(Locale.ROOT))) {
				suggestionList.add(new Suggestion(
					new StringRange(
						suggestion.getRange().getStart(),
						suggestion.getRange().getEnd() + currentInput.length()
					),
					suggestion.getText(),
					suggestion.getTooltip()
				));
				start = Math.min(suggestion.getRange().getStart(), start);
				end = Math.max(suggestion.getRange().getEnd(), end);
			}
		}
		return new Suggestions(new StringRange(start, end), suggestionList);
	}
}
