package adris.altoclef.commandsystem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public abstract class Command {

	protected static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
    protected static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;
    protected final static SimpleCommandExceptionType INCORRECT_USE = new SimpleCommandExceptionType(Text.literal("Incorrect command use!"));
    
    private final String _name;
    private final String _description;
    protected AltoClef _mod;
    private Runnable _onFinish = null;

    public Command(String name, String description, AltoClef mod) {
        _name = name;
        _description = description;
        _mod = mod;
    }

    public void run(AltoClef mod, String line, Runnable onFinish) throws CommandException {
        _onFinish = onFinish;
//        _mod = mod;
        try {
			CommandExecutor.dispatch(line);
		} catch (CommandSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    protected void finish() {
        if (_onFinish != null)
            //noinspection unchecked
            _onFinish.run();
    }
    
    public abstract void build(LiteralArgumentBuilder<CommandSource> builder);
    
    // Helper methods to painlessly infer the CommandSource generic type argument
    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }
    
    public final void registerTo(CommandDispatcher<CommandSource> dispatcher) {
        register(dispatcher, _name);
    }
    
    public void register(CommandDispatcher<CommandSource> dispatcher, String name) {
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(name);
        build(builder);
        dispatcher.register(builder);
    }

//    public String getHelpRepresentation() {
//        StringBuilder sb = new StringBuilder(_name);
//        for (ArgBase arg : REGISTRY_ACCESS.()) {
//            sb.append(" ");
//            sb.append(arg.getHelpRepresentation());
//        }
//        return sb.toString();
//    }

    protected void log(Object message) {
        Debug.logMessage(message.toString());
    }

    protected void logError(Object message) {
        Debug.logError(message.toString());
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }
    
    public String toString() {
    	
        return "@" + _name;
    }

    public String toString(String... args) {
        StringBuilder base = new StringBuilder(toString());
        for (String arg : args) base.append(' ').append(arg);
        return base.toString();
    }
}
