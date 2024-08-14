package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.MarvionBeatMinecraftTask;
import net.minecraft.command.CommandSource;

public class MarvionCommand extends Command {
    public MarvionCommand(AltoClef mod) {
        super("marvion", "Beats the game (Marvion version)", mod);
    }
    
    @Override
   	public void build(LiteralArgumentBuilder<CommandSource> builder) {
   		builder.executes(context -> {
   	        _mod.runUserTask(new MarvionBeatMinecraftTask(), this::finish);
   			return SINGLE_SUCCESS;
   		});
   		
   	}
}