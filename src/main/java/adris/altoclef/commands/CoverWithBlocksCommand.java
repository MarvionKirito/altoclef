package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.construction.CoverWithBlocksTask;
import net.minecraft.command.CommandSource;

public class CoverWithBlocksCommand extends Command {
    public CoverWithBlocksCommand(AltoClef mod) {
        super("coverwithblocks", "Cover nether lava with blocks", mod);
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
			_mod.runUserTask(new CoverWithBlocksTask(), this::finish);
			
			return SINGLE_SUCCESS;
		});
		
	}
}
