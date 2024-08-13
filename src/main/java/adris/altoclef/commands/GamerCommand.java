package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import net.minecraft.command.CommandSource;

public class GamerCommand extends Command {
    public GamerCommand(AltoClef mod) {
        super("gamer", "Beats the game", mod);
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
	        _mod.runUserTask(new BeatMinecraft2Task(), this::finish);
			return SINGLE_SUCCESS;
		});
		
	}
}
