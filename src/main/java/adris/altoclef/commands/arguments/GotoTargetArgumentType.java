package adris.altoclef.commands.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.GotoTarget;
import adris.altoclef.commandsystem.GotoTarget.GotoTargetCoordType;
import adris.altoclef.util.Dimension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class GotoTargetArgumentType implements ArgumentType<GotoTarget> {
    private static final GotoTargetArgumentType INSTANCE = new GotoTargetArgumentType();
    private static final Collection<String> EXAMPLES = List
	    .of("[x y z dimension]/[x z dimension]/[y dimension]/[dimension]/[x y z]/[x z]/[y]");
    static final DynamicCommandExceptionType INVALID_NUM_OF_COORD_ARGS = new DynamicCommandExceptionType(
	    numbers -> Text.literal("Unexpected number of integers passed to coordinate: " + numbers));

    public GotoTargetArgumentType() {
    }

    public static GotoTargetArgumentType create() {
	return INSTANCE;
    }

    public static GotoTarget get(CommandContext<?> context) {
	return context.getArgument("gotoTarget", GotoTarget.class);
    }

    @Override
    public GotoTarget parse(StringReader reader) throws CommandSyntaxException {
	reader.skipWhitespace();
//	        if (!reader.canRead()) {
//	            throw INVALID_NUM_OF_COORD_ARGS.create("");
//	        }
	final String text = reader.getRemaining();
	reader.setCursor(reader.getTotalLength());
	String[] parts = text.split(" ");
	List<Integer> numbers = new ArrayList<>();
	Dimension dimension = null;
	for (String part : parts) {
	    try {
		int num = Integer.parseInt(part);
		numbers.add(num);
	    } catch (NumberFormatException e) {
		try {
		    dimension = (Dimension) Arg.parseEnum(part, Dimension.class);
		} catch (CommandException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		    Debug.logWarning(e1.getMessage());
		    return null;
		}
		break;
	    }
	}
	int x = 0, y = 0, z = 0;
	GotoTarget.GotoTargetCoordType coordType;
	switch (numbers.size()) {
	case 0 -> coordType = GotoTargetCoordType.NONE;
	case 1 -> {
	    y = numbers.get(0);
	    coordType = GotoTargetCoordType.Y;
	}
	case 2 -> {
	    x = numbers.get(0);
	    z = numbers.get(1);
	    coordType = GotoTargetCoordType.XZ;
	}
	case 3 -> {
	    x = numbers.get(0);
	    y = numbers.get(1);
	    z = numbers.get(2);
	    coordType = GotoTargetCoordType.XYZ;
	}
	default -> throw INVALID_NUM_OF_COORD_ARGS.create(numbers.size());
	}
	;

	return new GotoTarget(x, y, z, dimension, coordType);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
	// [x y z dimension]/[x z dimension]/[y dimension]/[dimension]/[x y z]/[x z]/[y]
	try {
	    List<String> suggestions = new ArrayList<String>();

	    String remaining = builder.getRemaining();

	    Pattern pattern = Pattern.compile("^(\\d+ \\d+ \\d+)|(\\d+ \\d+)|(\\d+)", Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(remaining);
	    matcher.find();

	    if (!matcher.hasMatch()) {
		int x = MinecraftClient.getInstance().player.getBlockX();
		int y = MinecraftClient.getInstance().player.getBlockY();
		int z = MinecraftClient.getInstance().player.getBlockZ();
		suggestions.add(x + " " + y + " " + z);
		suggestions.add(x + " " + z);
		suggestions.add(y + "");

		for (String string : List.copyOf(suggestions)) {
		    for (Dimension dimension : Dimension.values()) {
			suggestions.add(string + " " + dimension.toString());
			suggestions.add(dimension.toString());
		    }
		}
	    } else if (matcher.hasMatch()) {
		String addon = matcher.group();
		suggestions.add(addon);
		for (Dimension dimension : Dimension.values()) {
		    suggestions.add(addon + " " + dimension.toString());
		}
	    }

	    return CommandSource.suggestMatching(suggestions, builder);
	} catch (ConcurrentModificationException e) {
	    return CommandSource.suggestMatching(new ArrayList<String>(), builder);
	}
    }

    @Override
    public Collection<String> getExamples() {
	return EXAMPLES;
    }
}
