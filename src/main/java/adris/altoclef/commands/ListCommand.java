package adris.altoclef.commands;

import java.util.Arrays;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.ui.MessagePriority;
import net.minecraft.command.CommandSource;

public class ListCommand extends Command {
    public ListCommand(AltoClef mod) {
        super("list", "List all obtainable items", mod);
    }

    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
	        _mod.log("#### LIST OF ALL OBTAINABLE ITEMS ####", MessagePriority.OPTIONAL);
	        _mod.log(Arrays.toString(TaskCatalogue.resourceNames().toArray()), MessagePriority.OPTIONAL);
	        _mod.log("############# END LIST ###############", MessagePriority.OPTIONAL);
			return SINGLE_SUCCESS;
		});
    }
}
