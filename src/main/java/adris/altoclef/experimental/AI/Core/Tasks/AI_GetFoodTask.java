package adris.altoclef.experimental.AI.Core.Tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.experimental.awareness.AwarenessSystem;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.stupid.TerminatorTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.StorageHelper;

//Shitty Test Task
public class AI_GetFoodTask extends Task {

    private final Task _foodTask = new CollectFoodTask(5);

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBehaviour().setForceFieldPlayers(true);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected String toDebugString() {
        return "[AI] get food";
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof AI_GetFoodTask;
    }


    @Override
    protected Task onTick(AltoClef mod) {
        //Get some food so we can last a little longer.
        if ((mod.getPlayer().getHungerManager().getFoodLevel() < (20 - 3 * 2)) && StorageHelper.calculateInventoryFoodScore(mod) <= 0) return _foodTask;
        return new AI_StartTask();
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return true;
    }
}
