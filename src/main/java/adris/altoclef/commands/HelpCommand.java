package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.ui.MessagePriority;
import net.minecraft.command.CommandSource;

public class HelpCommand extends Command {

    public HelpCommand(AltoClef mod) {
        super("help", "Lists all commands", mod);
    }
    
	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
			_mod.log("########## HELP: ##########", MessagePriority.OPTIONAL);
	        int padSize = 10;
	        for (Command c : AltoClef.getCommandExecutor().allCommands()) {
	            StringBuilder line = new StringBuilder();
	            //line.append("");
	            line.append(c.getName()).append(": ");
	            int toAdd = padSize - c.getName().length();
	            for (int i = 0; i < toAdd; ++i) {
	                line.append(" ");
	            }
	            line.append(c.getDescription());
	            _mod.log(line.toString(), MessagePriority.OPTIONAL);
	        }
	        _mod.log("###########################", MessagePriority.OPTIONAL);
	        finish();
	        return SINGLE_SUCCESS;
		});
	}
	
	
}
