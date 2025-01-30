package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.experimental.AI.Core.Tasks.AI_StartTask;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;

public class AICommand extends Command {
    public AICommand() {
        super("ai", "Experimental AI Command");
    }


    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new AI_StartTask(), this::finish);
    }

}
