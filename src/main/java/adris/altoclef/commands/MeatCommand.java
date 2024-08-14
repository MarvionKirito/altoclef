package adris.altoclef.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.resources.CollectMeatTask;
import net.minecraft.command.CommandSource;

public class MeatCommand extends Command {
    public MeatCommand(AltoClef mod) throws CommandException {
        super("meat", "Collects a certain amount of meat", mod);
    }
    
    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		// TODO Auto-generated method stub
		builder.then(argument("count", IntegerArgumentType.integer()).executes(context -> {
			int food = IntegerArgumentType.getInteger(context, "count");
	        _mod.runUserTask(new CollectMeatTask(food), this::finish);
	        return SINGLE_SUCCESS;
		}));
	}
}