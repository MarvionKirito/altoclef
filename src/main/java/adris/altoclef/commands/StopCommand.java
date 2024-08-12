package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import net.minecraft.command.CommandSource;

public class StopCommand extends Command {

    public StopCommand(AltoClef mod) {
        super("stop", "Stop task runner (stops all automation)", mod);
    }
    
    @Override
   	public void build(LiteralArgumentBuilder<CommandSource> builder) {
   		builder.executes(context -> {
   	        _mod.getUserTaskChain().cancel(_mod);
   	        finish();
               return SINGLE_SUCCESS;
   		});
   	}
}
