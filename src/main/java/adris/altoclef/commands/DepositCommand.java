package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.arguments.ItemListArgumentType;
import adris.altoclef.commandsystem.*;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import org.apache.commons.lang3.ArrayUtils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class DepositCommand extends Command {
    public DepositCommand(AltoClef mod) throws CommandException {
        super("deposit", "Deposit ALL of our items", mod
        		/*new Arg(ItemList.class, "items (empty for ALL non gear items)", null, 0, false)*/
        		);
    }

    public static ItemTarget[] getAllNonEquippedOrToolItemsAsTarget(AltoClef mod) {
        return StorageHelper.getAllInventoryItemsAsTargets(slot -> {
            // Ignore armor
            if (ArrayUtils.contains(PlayerSlot.ARMOR_SLOTS, slot))
                return false;
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            // Ignore tools
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                return !(item instanceof ToolItem);
            }
            return false;
        });
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("items", new ItemListArgumentType(REGISTRY_ACCESS)).executes(context -> {
			ItemList itemList = ItemListArgumentType.get(context);
			ItemTarget[] items = itemList.items;
            _mod.runUserTask(new StoreInAnyContainerTask(false, items), this::finish);
		        
			return SINGLE_SUCCESS;
		}));
		
		builder.executes(context -> {

			ItemTarget[] items = getAllNonEquippedOrToolItemsAsTarget(_mod);
            _mod.runUserTask(new StoreInAnyContainerTask(false, items), this::finish);
			
			return SINGLE_SUCCESS;
		});
		
	}
    
    
}
