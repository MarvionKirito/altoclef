package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.cheatmenu.UI.screens.clickgui.ClickGUI;
import adris.altoclef.tasksystem.Task;

public class OpenGUI extends Task {

    public boolean finished;
    @Override
    protected void onStart(AltoClef mod) {
        ClickGUI.open();
        finished = true;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return finished;
    }
}
