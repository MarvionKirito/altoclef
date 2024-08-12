package adris.altoclef.commands;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasksystem.Task;
import net.minecraft.command.CommandSource;

public class StatusCommand extends Command {
    public StatusCommand(AltoClef mod) {
        super("status", "Get status of currently executing command", mod);
    }

    @Override
   	public void build(LiteralArgumentBuilder<CommandSource> builder) {
   		builder.executes(context -> {
   	        List<Task> tasks = _mod.getUserTaskChain().getTasks();
   	        if (tasks.size() == 0) {
   	            _mod.log("No tasks currently running.");
   	        } else {
   	            _mod.log("CURRENT TASK: " + tasks.get(0).toString());
   	        }
   	        finish();
               return SINGLE_SUCCESS;
   		});
   	}
}