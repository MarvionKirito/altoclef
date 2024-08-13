package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commands.arguments.ItemListArgumentType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.util.ItemTarget;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class EquipCommand extends Command {
    public EquipCommand(AltoClef mod) throws CommandException {
        super("equip", "Equips armor", mod);
    }


	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(literal("leather").executes(context -> {
			ItemTarget[] items =
                    new ItemTarget[]{new ItemTarget(Items.LEATHER_HELMET),
                            new ItemTarget(Items.LEATHER_CHESTPLATE),
                            new ItemTarget(Items.LEATHER_LEGGINGS),
                            new ItemTarget(Items.LEATHER_BOOTS)};
			_mod.runUserTask(new EquipArmorTask(items), this::finish);
			return SINGLE_SUCCESS;
		}));
		
		builder.then(literal("iron").executes(context -> {

			ItemTarget[] items =
					new ItemTarget[]{new ItemTarget(Items.IRON_HELMET),
                            new ItemTarget(Items.IRON_CHESTPLATE),
                            new ItemTarget(Items.IRON_LEGGINGS),
                            new ItemTarget(Items.IRON_BOOTS)};
			_mod.runUserTask(new EquipArmorTask(items), this::finish);
					return SINGLE_SUCCESS;
				}));
		
		builder.then(literal("gold").executes(context -> {

			ItemTarget[] items =
					new ItemTarget[]{new ItemTarget(Items.GOLDEN_HELMET),
                            new ItemTarget(Items.GOLDEN_CHESTPLATE),
                            new ItemTarget(Items.GOLDEN_LEGGINGS),
                            new ItemTarget(Items.GOLDEN_BOOTS)};
			_mod.runUserTask(new EquipArmorTask(items), this::finish);
			return SINGLE_SUCCESS;
		}));
		
		builder.then(literal("diamond").executes(context -> {

			ItemTarget[] items =
					new ItemTarget[]{new ItemTarget(Items.DIAMOND_HELMET)
                            , new ItemTarget(Items.DIAMOND_CHESTPLATE),
                            new ItemTarget(Items.DIAMOND_LEGGINGS),
                            new ItemTarget(Items.DIAMOND_BOOTS)};
			_mod.runUserTask(new EquipArmorTask(items), this::finish);
			return SINGLE_SUCCESS;
		}));
		
		builder.then(literal("netherite").executes(context -> {

			ItemTarget[] items =
					new ItemTarget[]{new ItemTarget(Items.NETHERITE_HELMET), new ItemTarget(Items.NETHERITE_CHESTPLATE), new ItemTarget(Items.NETHERITE_LEGGINGS), new ItemTarget(Items.NETHERITE_BOOTS)};
			_mod.runUserTask(new EquipArmorTask(items), this::finish);
			return SINGLE_SUCCESS;
		}));
		
		builder.then(argument("items", new ItemListArgumentType(REGISTRY_ACCESS, (suggestionString) -> {
			
			for (Item item : TaskCatalogue.getItemMatches(suggestionString)) {
				if (!(item instanceof ArmorItem)) return false;
			}
			
			return TaskCatalogue.taskExists(suggestionString);
		})).executes(context -> {

			ItemTarget[] items = ItemListArgumentType.get(context).items;
			
			for (ItemTarget item : items) {
	            for (Item i : item.getMatches()) {
	                if (!(i instanceof ArmorItem)) {
	                    items = null; // flag items as "bad" if any of the items are not ArmorItems
	                    break;
	                }
	            }
	            if (items == null) {
	                break;
	            }
	        }
			
			if (items != null)
				_mod.runUserTask(new EquipArmorTask(items), this::finish);
			else
	            throw (new SimpleCommandExceptionType(Text.literal("You must provide armor items."))).create(); //inform the user that they can only use armor items.
	        	//TODO Possibly add in a variable to tell the user what was wrong. However, this is less helpful if a list of items is wrong.
			return SINGLE_SUCCESS;
		}));
	}
	
}
