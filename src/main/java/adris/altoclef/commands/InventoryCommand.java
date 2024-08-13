package adris.altoclef.commands;

import java.util.HashMap;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryCommand extends Command {
    public InventoryCommand(AltoClef mod) throws CommandException {
        super("inventory", "Prints the bot's inventory OR returns how many of an item the bot has", mod);
    }
    
    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("item", ItemStackArgumentType.itemStack(REGISTRY_ACCESS)).executes(context -> {
			String item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem().toString();
			Debug.logMessage(item.split(":")[1] );
            // Print item quantity
            Item[] matches = TaskCatalogue.getItemMatches(item.split(":")[1]);
            if (matches == null || matches.length == 0) {
            	_mod.logWarning("Item \"" + item + "\" is not catalogued/recognized.");
                finish();
                return SINGLE_SUCCESS;
            }
            int count = _mod.getItemStorage().getItemCount(matches);
            if (count == 0) {
            	_mod.log(item + " COUNT: (none)");
            } else {
            	_mod.log(item + " COUNT: " + count);
            }
	        finish();
			return SINGLE_SUCCESS;
		}));
		
		builder.executes(context -> {
            // Print inventory
            // Get item counts
            HashMap<String, Integer> counts = new HashMap<>();
            for (int i = 0; i < _mod.getPlayer().getInventory().size(); ++i) {
                ItemStack stack = _mod.getPlayer().getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String name = ItemHelper.stripItemName(stack.getItem());
                    if (!counts.containsKey(name)) counts.put(name, 0);
                    counts.put(name, counts.get(name) + stack.getCount());
                }
            }
            // Print
            _mod.log("INVENTORY: ", MessagePriority.OPTIONAL);
            for (String name : counts.keySet()) {
            	_mod.log(name + " : " + counts.get(name), MessagePriority.OPTIONAL);
            }
            _mod.log("(inventory list sent) ", MessagePriority.OPTIONAL);
	        finish();
			return SINGLE_SUCCESS;
		});
		
	}
}