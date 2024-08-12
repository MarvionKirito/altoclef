package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.arguments.PlayerListEntryArgumentType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.KillPlayerTask;
import net.minecraft.command.CommandSource;

public class PunkCommand extends Command {
    public PunkCommand(AltoClef mod) throws CommandException {
        super("punk", "Punk 'em", mod);
    }
    
    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
			String username = PlayerListEntryArgumentType.get(context).toString();
	        _mod.runUserTask(new KillPlayerTask(username), this::finish);
            return SINGLE_SUCCESS;
		}));
	}
}