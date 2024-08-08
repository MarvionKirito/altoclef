package adris.altoclef.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.CommandSource;

@Mixin(LiteralCommandNode.class)
public class LiteralCommandNodeMixin {
	@Shadow @Final private String literal;

	@Redirect(method = "listSuggestions", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"), remap = false)
	private boolean doShouldSuggestCheck(String literalLowerCase, String remainingLowerCase, CommandContext<?> context, SuggestionsBuilder builder) {
		return CommandSource.shouldSuggest(remainingLowerCase, literalLowerCase);
	}
}
