package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.construction.CoverWithSandTask;
import net.minecraft.command.CommandSource;

public class CoverWithSandCommand extends Command {
    public CoverWithSandCommand(AltoClef mod) {
        super("coverwithsand", "Cover nether lava with sand", mod);
    }
    
    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
			_mod.runUserTask(new CoverWithSandTask(), this::finish);
			
			return SINGLE_SUCCESS;
		});
		
	}
}
