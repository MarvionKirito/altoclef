package adris.altoclef.tasks.slot;

import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.slots.Slot;

public class SpreadItemToSlotsFromInventoryTask extends SpreadItemToSlotsTask {
    public SpreadItemToSlotsFromInventoryTask(ItemTarget toMove, Slot[] destinations) {
        super(toMove, destinations, mod -> mod.getItemStorage().getSlotsWithItemPlayerInventory(true, toMove.getMatches()));
    }
}
