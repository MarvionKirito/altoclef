package adris.altoclef.commands.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ItemList;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.text.Text;

public class ItemListArgumentType implements ArgumentType<ItemList> {
    private static final Collection<String> EXAMPLES = List.of("[stone,iron_sword]", "stick");
    private static final DynamicCommandExceptionType NO_SUCH_ITEMS = new DynamicCommandExceptionType(error -> Text.literal(error + ""));
	private final ItemStringReader reader;
	private final Predicate<String> predicate;

    public ItemListArgumentType(CommandRegistryAccess commandRegistryAccess) {

		this.reader = new ItemStringReader(commandRegistryAccess);
		this.predicate = (suggestionString) -> TaskCatalogue.taskExists(suggestionString);
    }
    
    public ItemListArgumentType(CommandRegistryAccess commandRegistryAccess, Predicate<String> predicate) {

		this.reader = new ItemStringReader(commandRegistryAccess);
		this.predicate = predicate;
		
    }
    
    public static ItemList get(CommandContext<?> context) {
        return context.getArgument("items", ItemList.class);
    }
    
	@Override
	public ItemList parse(StringReader reader) throws CommandSyntaxException {
		try {
			final String text = reader.getRemaining().startsWith("[") && !reader.getRemaining().contains("]") ? reader.getRemaining() + "]" : reader.getRemaining();
            reader.setCursor(reader.getTotalLength());
			return ItemList.parseRemainder(text);
		} catch (CommandException e) {
			throw NO_SUCH_ITEMS.create(e.getMessage());
		}
	}

	@Override
	public Collection<String> getExamples() {
	    return EXAMPLES;
	}
	
	@Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		
			List<Suggestion> suggestions;
	
			
			String[] spliInput = builder.getInput().split(" ");
			
			String remaining = "";
			String remainingAfterLastSpace = "";
			
			Pattern pattern = Pattern.compile("\\[(\\w+\\_{0,1}\\w+\\,)*", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher("");
			if (spliInput.length > 1) {
				remaining = builder.getInput().split(" ", 2)[1];
				remainingAfterLastSpace = spliInput[spliInput.length-1];
				matcher = pattern.matcher(remainingAfterLastSpace);
				matcher.find();
			}
			
			try {
				// I know this is a weird way to get item suggestions but I can't think of anything nicer so deal with it...
				suggestions = this.reader.getSuggestions(new SuggestionsBuilder("/give @a ", 9)).get().getList();
			
				List<String> suggestionStrings = new ArrayList<>();
				
				for (Suggestion suggestion : suggestions) {
					String suggestionString = suggestion.getText();
					if (suggestionString.split(":", 2).length > 1)
						suggestionString = suggestion.getText().split(":", 2)[1];
					if (predicate.test(suggestionString)) {
						if (spliInput.length > 1 && remainingAfterLastSpace.length() > 1 && matcher.hasMatch()) {
							suggestionStrings.add(matcher.group() + suggestionString);
						} else if (spliInput.length > 1 && remainingAfterLastSpace.length() > 0 && remainingAfterLastSpace.startsWith("[") && !(remainingAfterLastSpace.endsWith(","))) {
							suggestionStrings.add("[" + suggestionString);
						}  else {
							suggestionStrings.add(suggestionString);
						}
					}
					
				}
				
				
		        return CommandSource.suggestMatching(suggestionStrings, builder);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return CommandSource.suggestMatching(new ArrayList<>(), builder);
			}
    }
	
	
}
