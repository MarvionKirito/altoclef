package adris.altoclef.mixins;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import adris.altoclef.Debug;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Identifier;

import static adris.altoclef.commandsystem.suggestionsapi.Filtering.FilteringMode.*;

@Mixin(CommandSource.class)
public interface CommandSourceMixin {
	/**
	 * @author VelizarBG
	 * @reason Too niche to not overwrite
	 */
	@Overwrite
	static <T> void forEachMatching(Iterable<T> candidates, String remaining, Function<T, Identifier> getIdentifier, Consumer<T> action) {
//		Debug.logMessage("CommandSourceMixin - forEachMatching " + remaining);
//		Debug.logMessage("CommandSourceMixin - forEachMatching " + candidates.toString());
		for (T candidate : candidates) {
			Identifier identifier = getIdentifier.apply(candidate);

			if (CommandSource.shouldSuggest(remaining, identifier.toString()))
				action.accept(candidate);
		}
	}
	

	/**
	 * @author VelizarBG
	 * @reason Too niche to not overwrite
	 */
	@Overwrite
	static boolean shouldSuggest(String remaining, String candidate) {
		// if present, split namespace and path to allow searching inside a specific namespace
		String[] remainingId = remaining.split(":", 2);
		if (remainingId.length == 2)
			if (candidate.startsWith(remainingId[0]))
				remaining = remainingId[1];
			else
				return false;

		String[] candidateId = candidate.split(":", 2);
		if (candidateId.length == 2)
			candidate = candidateId[1];

		return VERY_LOOSE.test(remaining, candidate);
	}

	@Redirect(
		method = {
			"suggestIdentifiers(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Ljava/lang/String;)Ljava/util/concurrent/CompletableFuture;",
			"suggestIdentifiers(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;",
			"suggestFromIdentifier(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;",
			"suggestMatching(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;",
			"suggestMatching(Ljava/util/stream/Stream;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;",
			"method_9272(Ljava/lang/String;Ljava/lang/String;)Z",
			"suggestMatching([Ljava/lang/String;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;",
			"suggestMatching(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"
		},
		at = @At(value = "INVOKE", target = "Ljava/lang/String;toLowerCase(Ljava/util/Locale;)Ljava/lang/String;"))
	private static String handleCaseSensitivity(String string, Locale locale) {
		return string.toLowerCase(locale);
	}
}
