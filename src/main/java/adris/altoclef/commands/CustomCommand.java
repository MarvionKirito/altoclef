package adris.altoclef.commands;

import java.util.Arrays;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.util.helpers.ConfigHelper;
import net.minecraft.command.CommandSource;

public class CustomCommand extends Command {
    private static CustomTaskConfig _ctc;

    static {
        ConfigHelper.loadConfig("configs/CustomTasks.json", CustomTaskConfig::new, CustomTaskConfig.class, newConfig -> _ctc = newConfig);
    }


    public CustomCommand(AltoClef mod) throws CommandException {
        super(_ctc.prefix, "does a custom action", mod);
    }

    public static CustomTaskConfig getConfig() {
        return _ctc;
    }
    
    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {

	        String customCommand = context.getArgument("task", String.class);
	
	        StringBuilder commandToExecute = new StringBuilder();
	        int commandIndex = -1;
	        for (int i = 0; i < _ctc.customTasks.length; i++) {
	            if (_ctc.customTasks[i].name.equalsIgnoreCase(customCommand)) {
	                commandIndex = i;
	                break;
	            }
	        }
	        if (commandIndex > -1) {
	            for (int i = 0; i < _ctc.customTasks[commandIndex].tasks.length; i++) {
	                if (i > 0) {
	                    commandToExecute.append(";");
	                }
	                commandToExecute.append(_ctc.customTasks[commandIndex].tasks[i].command).append(" ");
	                if (_ctc.customTasks[commandIndex].tasks[i].command.equals("get") || _ctc.customTasks[commandIndex].tasks[i].command.equals("equip")) {
	                    //parameters have two inside arrays so we need to be careful here
	
	                    commandToExecute.append("[");
	                    for (int j = 0; j < _ctc.customTasks[commandIndex].tasks[i].parameters.length; j++) {
	                        commandToExecute.append(Arrays.toString(_ctc.customTasks[commandIndex].tasks[i].parameters[j]).replaceAll("\\[", "").replaceAll("]", "").replaceAll(",", ""));
	                        if (j < _ctc.customTasks[commandIndex].tasks[i].parameters.length - 1) {
	                            commandToExecute.append("?");
	
	                        }
	                    }
	                    commandToExecute.append("]");
	                } else {
	                    commandToExecute.append(Arrays.toString(_ctc.customTasks[commandIndex].tasks[i].parameters[0]).replaceAll("\\[", "").replaceAll("]", ""));
	                }
	            }
	            AltoClef.getCommandExecutor().execute(_mod.getModSettings().getCommandPrefix() + commandToExecute.toString().replaceAll(",", "").replaceAll("\\?", ","));
	        } else {
	
	        }
			
			return SINGLE_SUCCESS;
		});
		
	}
}
