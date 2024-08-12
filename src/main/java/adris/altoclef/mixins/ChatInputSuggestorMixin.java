package adris.altoclef.mixins;

import static adris.altoclef.commandsystem.suggestionsapi.Filtering.FilteringMode.LOOSE;
import static adris.altoclef.commandsystem.suggestionsapi.Filtering.FilteringMode.SLIGHTLY_LOOSE;
import static adris.altoclef.commandsystem.suggestionsapi.Filtering.FilteringMode.STRICT;

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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import adris.altoclef.AltoClef;
import adris.altoclef.Settings;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.util.helpers.ConfigHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Shadow private ParseResults<CommandSource> parse;
    @Shadow boolean completingSuggestions;
    @Shadow private ChatInputSuggestor.SuggestionWindow window;
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
    

    @Shadow
    protected abstract void showCommandSuggestions();

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


	@Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
	        cancellable = true,
	        locals = LocalCapture.CAPTURE_FAILHARD)
    private void inject(CallbackInfo ci, String string, StringReader reader) {
		String prefix = ConfigHelper.getConfig(Settings.SETTINGS_PATH, Settings::new, Settings.class).getCommandPrefix();
        int length = prefix.length();

        if (reader.canRead(length) && reader.getString().startsWith(prefix, reader.getCursor())) {
            String message = reader.getString();
            reader.setCursor(reader.getCursor() + length);

            if (this.parse == null) {
                AltoClef.getCommandExecutor();
                if (message.contains(";")) {
                	this.parse = CommandExecutor.DISPATCHER.parse(new StringReader(message.split(";")[message.split(";").length-1]), MinecraftClient.getInstance().getNetworkHandler().getCommandSource());
                } else
                	this.parse = CommandExecutor.DISPATCHER.parse(reader, MinecraftClient.getInstance().getNetworkHandler().getCommandSource());
            }

            int cursor = textField.getCursor();
            if (cursor >= length && (this.window == null || !this.completingSuggestions)) {
                AltoClef.getCommandExecutor();
                if (message.contains(";")) {
                	this.pendingSuggestions = CommandExecutor.DISPATCHER.getCompletionSuggestions(this.parse, new StringReader(message.split(";")[message.split(";").length-1]).getTotalLength());
                } else
                	this.pendingSuggestions = CommandExecutor.DISPATCHER.getCompletionSuggestions(this.parse, cursor);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.showCommandSuggestions();
                    }
                });
            }

            ci.cancel();
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