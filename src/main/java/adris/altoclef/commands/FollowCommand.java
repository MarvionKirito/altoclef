package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.arguments.PlayerListEntryArgumentType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.movement.FollowPlayerTask;
import net.minecraft.command.CommandSource;

public class FollowCommand extends Command {
    public FollowCommand(AltoClef mod) throws CommandException {
        super("follow", "Follows you or someone else", mod);
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
			String username = PlayerListEntryArgumentType.get(context).getProfile().getName();
			if (username == null) {
	            if (_mod.getButler().hasCurrentUser()) {
	                username = _mod.getButler().getCurrentUser();
	            } else {
	                _mod.logWarning("No butler user currently present. Running this command with no user argument can ONLY be done via butler.");
	                finish();
	                return SINGLE_SUCCESS;
	            }
	        }
	        _mod.runUserTask(new FollowPlayerTask(username), this::finish);
            return SINGLE_SUCCESS;
		}));
	}
}