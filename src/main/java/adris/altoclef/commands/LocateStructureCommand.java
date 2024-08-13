package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.movement.GoToStrongholdPortalTask;
import adris.altoclef.tasks.movement.LocateDesertTempleTask;
import net.minecraft.command.CommandSource;

public class LocateStructureCommand extends Command {

    public LocateStructureCommand(AltoClef mod) throws CommandException {
        super("locate_structure", "Locate a world generated structure.", mod);
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(literal("STRONGHOLD").executes(s -> {
            _mod.runUserTask(new GoToStrongholdPortalTask(1), this::finish);
			return SINGLE_SUCCESS;
		}));
		
		builder.then(literal("DESERT_TEMPLE").executes(s -> {
            _mod.runUserTask(new LocateDesertTempleTask(), this::finish);
			return SINGLE_SUCCESS;
		}));
	}
}