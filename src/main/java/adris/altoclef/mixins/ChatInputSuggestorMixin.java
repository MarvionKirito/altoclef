package adris.altoclef.mixins;

import static adris.altoclef.commandsystem.suggestionsapi.Filtering.FilteringMode.LOOSE;
import static adris.altoclef.commandsystem.suggestionsapi.Filtering.FilteringMode.SLIGHTLY_LOOSE;
import static adris.altoclef.commandsystem.suggestionsapi.Filtering.FilteringMode.STRICT;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Command;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
	@Shadow private static int getStartOfCurrentWord(String input) {
		throw new AssertionError();
	}
	@Shadow @Final TextFieldWidget textField;
	
	@Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    @Final
    private boolean slashOptional;
    
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(@)");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    

    @Shadow
    public abstract void show(boolean narrateFirstSuggestion);

	/**
	 * @author VelizarBG
	 * @reason Too niche to not overwrite
	 */
	@Overwrite
	private List<Suggestion> sortSuggestions(Suggestions suggestions) {
		String command = textField.getText().substring(0, textField.getCursor());
			
		// To make sorting command literals work
		if (command.startsWith("/") || command.startsWith("@"))
			command = command.substring(1);
		int startOfCurrentWord = getStartOfCurrentWord(command);
		String remaining = command.substring(startOfCurrentWord);
		// To make sorting tags work
		if (remaining.startsWith("#"))
			remaining = remaining.substring(1);
		List<Suggestion> strictList = Lists.newArrayList();
		List<Suggestion> slightlyLooseList = Lists.newArrayList();
		List<Suggestion> looseList = Lists.newArrayList();
		List<Suggestion> veryLooseList = Lists.newArrayList();

		if (remaining.contains(":"))
			remaining = remaining.substring(remaining.indexOf(':') + 1);
		remaining = remaining.toLowerCase(Locale.ROOT);

		for(Suggestion suggestion : suggestions.getList()) {
			String suggestionText = suggestion.getText();
			if (suggestionText.contains(":"))
				suggestionText = suggestionText.substring(suggestionText.indexOf(':') + 1);
			suggestionText = suggestionText.toLowerCase(Locale.ROOT);

			if (STRICT.test(remaining, suggestionText))
				strictList.add(suggestion);
			else if (SLIGHTLY_LOOSE.test(remaining, suggestionText))
				slightlyLooseList.add(suggestion);
			else if (LOOSE.test(remaining, suggestionText))
				looseList.add(suggestion);
			else
				veryLooseList.add(suggestion);
		}

		strictList.addAll(slightlyLooseList);
		strictList.addAll(looseList);
		strictList.addAll(veryLooseList);
		return strictList;
	}


	@Inject(method = "refresh", at = @At("TAIL"), cancellable = true)
    private void inject(CallbackInfo ci) {
		ArrayList<String> commands = new ArrayList<>();
		for (Command command : AltoClef.getCommandExecutor().allCommands()) {
            commands.add(command.getName());
        }
        String text = this.textField.getText();
        StringReader stringReader = new StringReader(text);
        boolean hasSlash = stringReader.canRead() && stringReader.peek() == '/';
        if (hasSlash) {
            stringReader.skip();
        }
        boolean isCommand = this.slashOptional || hasSlash;
        int cursor = this.textField.getCursor();
        if (!isCommand) {
            String textUptoCursor = text.substring(0, cursor);
            int start = Math.max(getLastPattern(textUptoCursor, COMMAND_PATTERN) - 1, 0);
            int whitespace = getLastPattern(textUptoCursor, WHITESPACE_PATTERN);
            if (start < textUptoCursor.length() && start >= whitespace) {
                if (textUptoCursor.charAt(start) == '@') {
                    this.pendingSuggestions = CommandSource.suggestMatching(commands, new SuggestionsBuilder(textUptoCursor, start));
                    this.pendingSuggestions.thenRun(() -> {
                        if (!this.pendingSuggestions.isDone()) {
                            return;
                        }
                        this.show(false);
                    });
                    ci.cancel();
                }
            }
        }
    }
	
	 private int getLastPattern(String input, Pattern pattern) {
	        if (Strings.isNullOrEmpty(input)) {
	            return 0;
	        }
	        int i = 0;
	        Matcher matcher = pattern.matcher(input);
	        while (matcher.find()) {
	            i = matcher.end();
	        }
	        return i;
	    }
}