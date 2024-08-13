package adris.altoclef.tasks.misc;

import java.util.List;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class GetPickaxesTask extends Task
{

	private Task _getPicks = TaskCatalogue.getItemTask(new ItemTarget(Items.IRON_PICKAXE, 4));

	public GetPickaxesTask()
	{

	}

	@Override
	protected void onStart(AltoClef mod)
	{
		// TODO Auto-generated method stub
		if (mod.getItemStorage().getItemCount(Items.IRON_PICKAXE) == 1)
		{
			_getPicks = TaskCatalogue.getItemTask(new ItemTarget(Items.IRON_PICKAXE, 5));
		}

	}

	@Override
	protected Task onTick(AltoClef mod)
	{
		Item[] PICKAXES = new Item[] {
				Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
				Items.GOLDEN_PICKAXE
		};
		if (mod.getItemStorage().getSlotsWithItemScreen(PICKAXES).size() == 0)
		{
			return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 2);
		}
		List<Slot> stonePickSlots = mod.getItemStorage().getSlotsWithItemScreen(Items.STONE_PICKAXE);
		if (stonePickSlots.size() == 1 && stonePickSlots.get(0).getInventorySlot() > -1
				&& mod.getItemStorage().getItemStacksPlayerInventory(false)
						.get(stonePickSlots.get(0).getInventorySlot())
						.getDamage() > (Items.STONE_PICKAXE.getDefaultStack().getMaxDamage() * 0.6))
		{
			return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 2);
		}

		return _getPicks;
	}

	@Override
	protected void onStop(AltoClef mod, Task interruptTask)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean isEqual(Task other)
	{
		// TODO Auto-generated method stub
		return other instanceof GetPickaxesTask;
	}

	@Override
	protected String toDebugString()
	{
		// TODO Auto-generated method stub
		return "Gathering pickaxes for long mining journey!";
	}

	@Override
	public boolean isFinished(AltoClef mod)
	{
		return _getPicks.isFinished(mod);
	}

}
