package adris.altoclef.tasks.slot;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class SpreadItemToSlotsTask extends Task {

    private final ItemTarget _toMove;
    private final Slot[] _slotsToSpreadTo;
    private final Function<AltoClef, List<Slot>> _getMovableSlots;

    public SpreadItemToSlotsTask(ItemTarget toMove, Slot[] slotsToSpreadTo, Function<AltoClef, List<Slot>> getMovableSlots) {
        _toMove = toMove;
        _slotsToSpreadTo = slotsToSpreadTo;
        _getMovableSlots = getMovableSlots;
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getSlotHandler().canDoSlotAction()) {
        	for (Slot _destination : _slotsToSpreadTo) {
	            // Rough plan
	            // - If empty slot or wrong item
	            //      Find best matching item (smallest count over target, or largest count if none over)
	            //      Click on it (one turn)
	            // - If held slot has < items than target count
	            //      Left click on destination slot (one turn)
	            // - If held slot has > items than target count
	            //      Right click on destination slot (one turn)
	            ItemStack currentHeld = StorageHelper.getItemStackInCursorSlot();
	            ItemStack atTarget = StorageHelper.getItemStackInSlot(_destination);
	
	            // Items that CAN be moved to that slot.
	            Item[] validItems = _toMove.getMatches();//Arrays.stream(_toMove.getMatches()).filter(item -> mod.getItemStorage().getItemCount(item) >= _toMove.getTargetCount()).toArray(Item[]::new);
	
	            // We need to deal with our cursor stack OR put an item there (to move).
	            boolean wrongItemHeld = !Arrays.asList(validItems).contains(currentHeld.getItem());
	            if (currentHeld.isEmpty() || wrongItemHeld) {
	                Optional<Slot> toPlace;
	                if (currentHeld.isEmpty()) {
	                    // Just pick up
	                    toPlace = getBestSlotToPickUp(mod, validItems);
	                } else {
	                    // Try to fit the currently held item first.
	                    toPlace = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(currentHeld, true);
	                    if (toPlace.isEmpty()) {
	                        // If all else fails, just swap it.
	                        toPlace = getBestSlotToPickUp(mod, validItems);
	                    }
	                }
	                if (toPlace.isEmpty()) {
	                    Debug.logError("Called SpreadItemToSlots when item/not enough item is available! valid items: " + StlHelper.toString(validItems, Item::getTranslationKey));
	                    return null;
	                }
	                mod.getSlotHandler().clickSlot(toPlace.get(), 0, SlotActionType.PICKUP);
	                return null;
	            }
	
	            int currentlyPlaced = Arrays.asList(validItems).contains(atTarget.getItem()) ? atTarget.getCount() : 0;
		            if(currentHeld.getItem().getMaxCount() == 1) {
		            	return new MoveItemToSlotTask(_toMove, _destination, _getMovableSlots);
		            }
	                // Place one at a time.
	            	ClientPlayerEntity player = MinecraftClient.getInstance().player;
	            	if (player == null) return null;
	                int syncId = player.currentScreenHandler.syncId;
	                // Minecraft seems to send this funky packet before the slots you actually want to spread to... IDK why, Mojang is weird like that
	                mod.getController().clickSlot(syncId, -999, 0, SlotActionType.QUICK_CRAFT, player);
	                for (Slot craftSlot : _slotsToSpreadTo) {
	                	mod.getSlotHandler().clickSlotForce(craftSlot, 1, SlotActionType.QUICK_CRAFT);
					}
	                // Minecraft seems to send this funky packet after the slots you actually want to spread to... IDK why, Mojang is weird like that
	                mod.getController().clickSlot(syncId, -999, 2, SlotActionType.QUICK_CRAFT, player);
			}
            return null;
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    public boolean isFinished(AltoClef mod) {
    	for (Slot slot : _slotsToSpreadTo) {
            ItemStack atDestination = StorageHelper.getItemStackInSlot(slot);
            if (!(_toMove.matches(atDestination.getItem()) && (atDestination.getCount() >= _toMove.getTargetCount() || atDestination.getMaxCount() > 1))) return false;
		}
        return true;
    }

    @Override
    protected boolean isEqual(Task obj) {
        if (obj instanceof SpreadItemToSlotsTask task) {
            return task._toMove.equals(_toMove) && task._slotsToSpreadTo.equals(_slotsToSpreadTo);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Spreading " + _toMove + " to " + _slotsToSpreadTo.toString();
    }

    private Optional<Slot> getBestSlotToPickUp(AltoClef mod, Item[] validItems) {
        Slot bestMatch = null;
        if (!_getMovableSlots.apply(mod).isEmpty()) {
            for (Slot slot : _getMovableSlots.apply(mod)) {
                if (Slot.isCursor(slot))
                    continue;
                if (!_toMove.matches(StorageHelper.getItemStackInSlot(slot).getItem()))
                    continue;
                if (bestMatch == null) {
                    bestMatch = slot;
                    continue;
                }
                int countBest = StorageHelper.getItemStackInSlot(bestMatch).getCount();
                int countCheck = StorageHelper.getItemStackInSlot(slot).getCount();
                if ((countBest < _toMove.getTargetCount() && countCheck > countBest)
                        || (countBest >= _toMove.getTargetCount() && countCheck >= _toMove.getTargetCount() && countCheck > countBest)) {
                    // If we don't have enough, go for largest
                    // If we have too much, go for smallest over the limit.
                    bestMatch = slot;
                }
            }
        }
        return Optional.ofNullable(bestMatch);
    }
}