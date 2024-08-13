package adris.altoclef.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.resources.CollectFoodTask;
import net.minecraft.command.CommandSource;

public class FoodCommand extends Command {
    public FoodCommand(AltoClef mod) throws CommandException {
        super("food", "Collects a certain amount of food", mod);
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		// TODO Auto-generated method stub
		builder.then(argument("count", IntegerArgumentType.integer(1)).executes(context -> {
			try {

				int food = IntegerArgumentType.getInteger(context, "count");
		        _mod.runUserTask(new CollectFoodTask(food), this::finish);
			} catch (IllegalArgumentException e) {
				throw INCORRECT_USE.create();
			}
            return SINGLE_SUCCESS;
		}));
	}
}