package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.command.CommandSource;

public class CoordsCommand extends Command {
    public CoordsCommand(AltoClef mod) {
        super("coords", "Get the bot's current coordinates", mod);
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
	        _mod.log("CURRENT COORDINATES: " + _mod.getPlayer().getBlockPos().toShortString() + " (Current dimension: " + WorldHelper.getCurrentDimension() + ")");
	        return SINGLE_SUCCESS;
		});
		
	}
}
