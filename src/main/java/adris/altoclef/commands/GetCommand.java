package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commands.arguments.ItemListArgumentType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ItemList;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.ItemTarget;
import net.minecraft.command.CommandSource;

public class GetCommand extends Command {

    public GetCommand(AltoClef mod) throws CommandException {
        super("get", "Get an item/resource", mod);
    }

    @SuppressWarnings("unused")
	private static void OnResourceDoesNotExist(AltoClef mod, String resource) {
        mod.log("\"" + resource + "\" is not a catalogued resource. Can't get it yet, sorry! If it's a generic block try using baritone.", MessagePriority.OPTIONAL);
        mod.log("Use @list to get a list of available resources.", MessagePriority.OPTIONAL);
    }

    private void GetItems(AltoClef mod, ItemTarget... items) {
        Task targetTask;
        if (items == null || items.length == 0) {
            mod.log("You must specify at least one item!");
            finish();
            return;
        }
        if (items.length == 1) {
            targetTask = TaskCatalogue.getItemTask(items[0]);
        } else {
            targetTask = TaskCatalogue.getSquashedItemTask(items);
        }
        if (targetTask != null) {
            mod.runUserTask(targetTask, this::finish);
        } else {
            finish();
        }
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("items", new ItemListArgumentType(REGISTRY_ACCESS)).executes(context -> {
			ItemList items = ItemListArgumentType.get(context);
	        GetItems(_mod, items.items);
			return SINGLE_SUCCESS;
		}));
		
//		builder.then(argument("items", StringArgumentType.greedyString()).executes(context -> {
//			ItemList items;
//			try {
//				items = ItemList.parseRemainder(StringArgumentType.getString(context, "items"));
//		        GetItems(_mod, items.items);
//			} catch (CommandException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return SINGLE_SUCCESS;
//		}));
		
//		builder.then(argument("item", ItemStackArgumentType.itemStack(REGISTRY_ACCESS)).executes(context -> {
//
//			Debug.logMessage(ItemStackArgumentType.getItemStackArgument(context, "item").asString(REGISTRY_ACCESS));
//			return SINGLE_SUCCESS;
//		}));
		
	}
}