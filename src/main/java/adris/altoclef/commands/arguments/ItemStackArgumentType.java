package adris.altoclef.commands.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import adris.altoclef.TaskCatalogue;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;

public class ItemStackArgumentType implements ArgumentType<ItemStackArgument> {

	private static final Collection<String> EXAMPLES = Arrays.asList("stick");
	private final ItemStringReader reader;

	public ItemStackArgumentType(CommandRegistryAccess commandRegistryAccess) {
		this.reader = new ItemStringReader(commandRegistryAccess);
	}

	public static ItemStackArgumentType itemStack(CommandRegistryAccess commandRegistryAccess) {
		return new ItemStackArgumentType(commandRegistryAccess);
	}

	public ItemStackArgument parse(StringReader stringReader) throws CommandSyntaxException {
		ItemStringReader.ItemResult itemResult = this.reader.consume(stringReader);
		return new ItemStackArgument(itemResult.item(), itemResult.components());
	}

	public static <S> String getItemStackArgument(CommandContext<S> context, String name) {
		return name;
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
	
	@Override
	 public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		
		List<Suggestion> suggestions;
		try {
			suggestions = this.reader.getSuggestions(builder).get().getList();
		
			List<String> suggestionStrings = new ArrayList<>();
			
//			String[] parts = context.getInput().split(" ", 2);
//			String add = "";
//			if (parts.length > 1) add = parts[1];
			
			
			for (Suggestion suggestion : suggestions) {
				String suggestionString = suggestion.getText();
				if (suggestionString.split(":", 2).length > 1)
					suggestionString = suggestion.getText().split(":", 2)[1];
				if (TaskCatalogue.taskExists(suggestionString))
					suggestionStrings.add(suggestionString);
				
			}
			
			
	        return CommandSource.suggestMatching(suggestionStrings, builder);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return CommandSource.suggestMatching(new ArrayList<>(), builder);
		}
}

}
