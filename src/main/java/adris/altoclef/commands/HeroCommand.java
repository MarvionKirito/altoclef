package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.entity.HeroTask;
import net.minecraft.command.CommandSource;

public class HeroCommand extends Command {
    public HeroCommand(AltoClef mod) {
        super("hero", "Kill all hostile mobs", mod);
    }

    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
	        _mod.runUserTask(new HeroTask(), this::finish);
			return SINGLE_SUCCESS;
		});
		
	}
}
