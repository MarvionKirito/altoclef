package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;

public class Gamer2Command extends Command {
    public Gamer2Command() {
        super("gamer2", "Beats the game (deprecated)");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new BeatMinecraft2Task(), this::finish);
    }
}
