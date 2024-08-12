package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.entity.SelfCareTask;
import net.minecraft.command.CommandSource;

public class SelfCareCommand extends Command {
    public SelfCareCommand(AltoClef mod) {
        super("selfcare", "Care for self (Not finished and tested yet)", mod);
    }
    
    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
	        _mod.runUserTask(new SelfCareTask(), this::finish);
            return SINGLE_SUCCESS;
		});
	}
}
