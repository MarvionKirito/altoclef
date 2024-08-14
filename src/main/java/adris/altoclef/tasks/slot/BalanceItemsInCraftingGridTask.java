package adris.altoclef.tasks.slot;

import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.CraftingTableSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class BalanceItemsInCraftingGridTask extends Task {
	
	private final int _requiredPerSlot;
	private final Slot _slotToBalance;
	private final ItemTarget _toFill;
	
	public BalanceItemsInCraftingGridTask(ItemTarget toFill, int requiredPerSlot, Slot slotToBalance) {
		_toFill = toFill;
		_requiredPerSlot = requiredPerSlot;
		_slotToBalance = slotToBalance;
	}

	@Override
	protected void onStart(AltoClef mod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Task onTick(AltoClef mod) {
		ItemStack present = StorageHelper.getItemStackInSlot(_slotToBalance);
		boolean oversatisfies = present.getCount() > _requiredPerSlot;
		if (oversatisfies) {
			ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
			if (!cursorStack.isEmpty()) {
				Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false, false);
				setDebugState("Ensuring cursor is empty");
				if (moveTo.isPresent()) {
					mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
					return null;
				}
				if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
					mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
					return null;
				}
				Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
				// Try throwing away cursor slot if it's garbage
				if (garbage.isPresent()) {
					mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
					return null;
				}
				mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
				return null;
			}
			mod.getSlotHandler().clickSlot(_slotToBalance, 1, SlotActionType.PICKUP);
			present = StorageHelper.getItemStackInSlot(_slotToBalance);
			oversatisfies = present.getCount() > _requiredPerSlot;
		}
		boolean correctItem = _toFill.matches(present.getItem());
		boolean isSatisfied = correctItem && present.getCount() == _requiredPerSlot;
		if(!isSatisfied) {
			setDebugState("Refilling");
			return new MoveItemToSlotFromInventoryTask(new ItemTarget(_toFill, _requiredPerSlot),
					_slotToBalance);
		}
		return null;
	}

	@Override
	protected void onStop(AltoClef mod, Task interruptTask) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isEqual(Task obj) {
        if (obj instanceof BalanceItemsInCraftingGridTask task) {
            return task._toFill.equals(_toFill) && task._slotToBalance.equals(_slotToBalance);
        }
        return false;
    }
	
	@Override
    public boolean isFinished(AltoClef mod) {
        return StorageHelper.getItemStackInSlot(_slotToBalance).getCount()  == _requiredPerSlot;
    }

	@Override
	protected String toDebugString() {
		return "OVER SATISFIED slot " + _slotToBalance.getInventorySlot() + "! Balancing slots!";
	}

}
