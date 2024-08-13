package adris.altoclef.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.slot.BalanceItemsInCraftingGridTask;
import adris.altoclef.tasks.slot.EnsureFreeCursorSlotTask;
import adris.altoclef.tasks.slot.MoveItemToSlotFromInventoryTask;
import adris.altoclef.tasks.slot.ReceiveCraftingOutputSlotTask;
import adris.altoclef.tasks.slot.SpreadItemToSlotsFromInventoryTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.CraftingTableSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Assuming a crafting screen is open, crafts a recipe.
 * <p>
 * Not useful for custom tasks.
 */
public class CraftGenericManuallyTask extends Task {

	private final RecipeTarget _target;
	private BalanceItemsInCraftingGridTask _balanceItemsInCraftingGridTask = null;

	public CraftGenericManuallyTask(RecipeTarget target) {
		_target = target;
	}

	@Override
	protected void onStart(AltoClef mod) {

	}

	@Override
	protected Task onTick(AltoClef mod) {

		boolean bigCrafting = StorageHelper.isBigCraftingOpen();

		if (!bigCrafting && !StorageHelper.isPlayerInventoryOpen()) {
			// Make sure we're not in another screen before we craft,
			// otherwise crafting won't work
			ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
			if (!cursorStack.isEmpty()) {
				Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
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
			} else {
				StorageHelper.closeScreen();
			}
			// Just to be safe
		}

		Slot outputSlot = bigCrafting ? CraftingTableSlot.OUTPUT_SLOT : PlayerSlot.CRAFT_OUTPUT_SLOT;

		// Example:
		// We need 9 sticks
		// plank recipe results in 4 sticks
		// this means 3 planks per slot
		// BUT if we already have 7 sticks then we only need 1 plank per slot
		int requiredPerSlot = (int) Math
				.ceil((double) (_target.getTargetCount() - mod.getItemStorage().getItemCount(_target.getOutputItem()))
						/ _target.getRecipe().outputCount());
		if (requiredPerSlot > 64)
			requiredPerSlot = 64;
		

		if (_balanceItemsInCraftingGridTask == null || !_balanceItemsInCraftingGridTask.isActive() && _balanceItemsInCraftingGridTask.isFinished(mod)) {

		if (mod.getModSettings().shouldSpreadItemsToCraft() && _target.getRecipe().getFilledSlotCount() != 1) {
			for (int craftSlot = 0; craftSlot < _target.getRecipe().getSlotCount(); ++craftSlot) {
				List<Slot> slots = new ArrayList<Slot>();
				ItemTarget toFill = _target.getRecipe().getSlot(craftSlot);
				Slot currentCraftSlot;
				if (bigCrafting) {
					// Craft in table
					currentCraftSlot = CraftingTableSlot.getInputSlot(craftSlot,
							_target.getRecipe().isBig());
				} else {
					// Craft in window
					currentCraftSlot = PlayerSlot.getCraftInputSlot(craftSlot);
				}
				ItemStack present = StorageHelper.getItemStackInSlot(currentCraftSlot);
				if (toFill == null || toFill.isEmpty() || !toFill.matches(present.getItem()))
				{
					if (present.getItem() != Items.AIR && !toFill.matches(present.getItem())) {
						if(!StorageHelper.getItemStackInCursorSlot().isEmpty())
						{
							return new EnsureFreeCursorSlotTask();
						}
						// Move this item OUT if it should be empty
						setDebugState("Found INVALID slot");
						mod.getSlotHandler().clickSlot(currentCraftSlot, 0, SlotActionType.PICKUP);
					}
				}
				for (int craftSlotInner = 0; craftSlotInner < _target.getRecipe().getSlotCount(); ++craftSlotInner) {
					ItemTarget toFillInner = _target.getRecipe().getSlot(craftSlotInner);
					if (!toFill.equals(toFillInner) || toFillInner == null || toFillInner.isEmpty())
						continue;
			        // Ensure our cursor is empty/can receive our item
			        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
			        if (!ItemHelper.canStackTogether(StorageHelper.getItemStackInSlot(outputSlot), cursor)) {
			            Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
			            if (toFit.isPresent()) {
			                mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
			            } else if(ItemHelper.canThrowAwayStack(mod, cursor)) {
			                // Eh screw it
			                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
			            }
			        }

					Slot currentCraftSlotInner;
					if (bigCrafting) {
						// Craft in table
						currentCraftSlotInner = CraftingTableSlot.getInputSlot(craftSlotInner,
								_target.getRecipe().isBig());
					} else {
						// Craft in window
						currentCraftSlotInner = PlayerSlot.getCraftInputSlot(craftSlotInner);
					}
					ItemStack presentInner = StorageHelper.getItemStackInSlot(currentCraftSlotInner);
					boolean correctItem = toFillInner.matches(presentInner.getItem());
					boolean isSatisfied = correctItem && (presentInner.getCount() >= requiredPerSlot || presentInner.getMaxCount() == 1);
					if (isSatisfied)
						continue;
					boolean oversatisfies = present.getCount() > requiredPerSlot;
					if (oversatisfies) {
						if(!StorageHelper.getItemStackInCursorSlot().isEmpty())
						{
							return new EnsureFreeCursorSlotTask();
						}
						setDebugState(
								"OVER SATISFIED slot! Right clicking slot to extract half and spread it out more.");
						mod.getSlotHandler().clickSlot(currentCraftSlot, 0, SlotActionType.PICKUP);
					}
					slots.add(currentCraftSlotInner);
				}
				if (slots.size() > 0) {
					return new SpreadItemToSlotsFromInventoryTask(new ItemTarget(toFill, requiredPerSlot),
							slots.toArray(new Slot[0]));
				}
			}
		} else {
			// For each slot in table
			for (int craftSlot = 0; craftSlot < _target.getRecipe().getSlotCount(); ++craftSlot) {
				ItemTarget toFill = _target.getRecipe().getSlot(craftSlot);
				Slot currentCraftSlot;
				if (bigCrafting) {
					// Craft in table
					currentCraftSlot = CraftingTableSlot.getInputSlot(craftSlot, _target.getRecipe().isBig());
				} else {
					// Craft in window
					currentCraftSlot = PlayerSlot.getCraftInputSlot(craftSlot);
				}
				ItemStack present = StorageHelper.getItemStackInSlot(currentCraftSlot);
				if (toFill == null || toFill.isEmpty()) {
					if (present.getItem() != Items.AIR) {
						// Move this item OUT if it should be empty
						setDebugState("Found INVALID slot");
						mod.getSlotHandler().clickSlot(currentCraftSlot, 0, SlotActionType.PICKUP);
					}
				} else {
					boolean correctItem = toFill.matches(present.getItem());
					boolean isSatisfied = correctItem && (present.getCount() >= requiredPerSlot || present.getMaxCount() == 1);
					if (!isSatisfied) {
						// We have items that satisfy, but we CAN NOT fill in the current slot!
						// In that case, just grab from the output.
						if (!mod.getItemStorage().hasItemInventoryOnly(present.getItem())) {
							if (!StorageHelper.getItemStackInSlot(outputSlot).isEmpty()) {
								setDebugState("NO MORE to fit: grabbing from output.");
								return new ReceiveCraftingOutputSlotTask(outputSlot, _target.getTargetCount());
							} else {
								// Move on to the NEXT slot, we can't fill this one anymore.
								continue;
							}
						}

						setDebugState("Moving item to slot...");
						return new MoveItemToSlotFromInventoryTask(new ItemTarget(toFill, requiredPerSlot),
								currentCraftSlot);
					}
					// We could be OVER satisfied
					boolean oversatisfies = present.getCount() > requiredPerSlot;
					if (oversatisfies) {
						setDebugState(
								"OVER SATISFIED slot! Right clicking slot to extract half and spread it out more.");
						mod.getSlotHandler().clickSlot(currentCraftSlot, 0, SlotActionType.PICKUP);
					}
				}
			}
		}
		}

		if (requiredPerSlot != 1)
			for (int craftSlot = 0; craftSlot < _target.getRecipe().getSlotCount(); ++craftSlot) {
				ItemTarget toFill = _target.getRecipe().getSlot(craftSlot);
				Slot currentCraftSlot;
				if (bigCrafting) {
					// Craft in table
					currentCraftSlot = CraftingTableSlot.getInputSlot(craftSlot, _target.getRecipe().isBig());
				} else {
					// Craft in window
					currentCraftSlot = PlayerSlot.getCraftInputSlot(craftSlot);
				}
				ItemStack present = StorageHelper.getItemStackInSlot(currentCraftSlot);
				boolean oversatisfies = present.getCount() > requiredPerSlot;
				if (oversatisfies || _balanceItemsInCraftingGridTask != null && !_balanceItemsInCraftingGridTask.isFinished(mod)) {
					_balanceItemsInCraftingGridTask = new BalanceItemsInCraftingGridTask(toFill, requiredPerSlot, currentCraftSlot);
					return _balanceItemsInCraftingGridTask;
				} else if (toFill != null && !toFill.isEmpty()) {
					break;
				}
			}
		else _balanceItemsInCraftingGridTask = null;

		// Ensure our cursor is empty/can receive our item
		ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
		if (!ItemHelper.canStackTogether(StorageHelper.getItemStackInSlot(outputSlot), cursor)) {
			Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false)
					.or(() -> StorageHelper.getGarbageSlot(mod));
			if (toFit.isPresent()) {
				mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
			} else if (ItemHelper.canThrowAwayStack(mod, cursor)) {
				// Eh screw it
				mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
			}
		}

		if (!StorageHelper.getItemStackInSlot(outputSlot).isEmpty() && StorageHelper.getItemStackInSlot(outputSlot).getItem().equals(_target.getOutputItem())) {
			return new ReceiveCraftingOutputSlotTask(outputSlot, _target.getTargetCount());
		} else {
			// Wait
			return null;
		}
	}

	@Override
	protected void onStop(AltoClef mod, Task interruptTask) {

	}

	@Override
	protected boolean isEqual(Task other) {
		if (other instanceof CraftGenericManuallyTask task) {
			return task._target.equals(_target);
		}
		return false;
	}

	@Override
	protected String toDebugString() {
		return "Crafting: " + _target;
	}
}
