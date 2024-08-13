package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.movement.IdleTask;
import net.minecraft.command.CommandSource;

public class IdleCommand extends Command {
    public IdleCommand(AltoClef mod) {
        super("idle", "Stand still", mod);
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
			_mod.runUserTask(new IdleTask(), this::finish);
			return SINGLE_SUCCESS;
		});
		
	}
}
