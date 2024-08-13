package adris.altoclef.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

public class SetGammaCommand extends Command {

    public SetGammaCommand(AltoClef mod) throws CommandException {
        super("gamma", "Sets the brightness to a value", mod);
    }
    
    @SuppressWarnings("resource")
	@Override
   	public void build(LiteralArgumentBuilder<CommandSource> builder) {
   		builder.then(argument("gamma", DoubleArgumentType.doubleArg()).executes(context -> {
	        double gammaValue = DoubleArgumentType.getDouble(context, "gamma");
	        Debug.logMessage("Gamma set to " + gammaValue);
	        MinecraftClient.getInstance().options.getGamma().setValue(gammaValue);
           return SINGLE_SUCCESS;
		}));
   	}
}
