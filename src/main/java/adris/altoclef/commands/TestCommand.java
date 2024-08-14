package adris.altoclef.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.Playground;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import net.minecraft.command.CommandSource;

public class TestCommand extends Command {

    public TestCommand(AltoClef mod) throws CommandException {
        super("test", "Generic command for testing", mod);
    }
    
    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("extra", StringArgumentType.word()).executes(context -> {
			String username = StringArgumentType.getString(context, "extra");
	        Playground.TEMP_TEST_FUNCTION(_mod, username);
            return SINGLE_SUCCESS;
		}));
	}
}