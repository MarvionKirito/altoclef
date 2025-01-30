package adris.altoclef.experimental.AI.Core.Tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;

// Currently Does Nothing
// Need to use Test Scores to determine which Task should be runnable
public class AI_StartTask extends Task {


    @Override
    protected String toDebugString() {
        return "[AI] start";
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof AI_StartTask;
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        return new AI_GetFoodTask();
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }
}
