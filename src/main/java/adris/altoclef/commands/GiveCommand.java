package adris.altoclef.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commands.arguments.ItemStackArgumentType;
import adris.altoclef.commands.arguments.PlayerListEntryArgumentType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.GiveItemToPlayerTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;

public class GiveCommand extends Command {
	public GiveCommand(AltoClef mod) throws CommandException {
		super("give", "Collects an item and gives it to you or someone else",
				/*
				 * new Arg(String.class, "username", null, 2), new Arg(String.class, "item"),
				 * new Arg(Integer.class, "count", 1, 1)
				 */
				mod);
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("player", PlayerListEntryArgumentType.create())
				.then(argument("item", ItemStackArgumentType.itemStack(REGISTRY_ACCESS))
						.then(argument("count", IntegerArgumentType.integer()).executes(context -> {
							String item = ItemStackArgumentType.getItemStackArgument(context, "item");
							int count = IntegerArgumentType.getInteger(context, "count");

							String username = PlayerListEntryArgumentType.get(context).toString();
							if (username == null) {
								if (_mod.getButler().hasCurrentUser()) {
									username = _mod.getButler().getCurrentUser();
								} else {
									_mod.logWarning(
											"No butler user currently present. Running this command with no user argument can ONLY be done via butler.");
									finish();
									return SINGLE_SUCCESS;
								}
							}
							ItemTarget target = null;
							if (TaskCatalogue.taskExists(item)) {
								// Registered item with task.
								target = TaskCatalogue.getItemTarget(item, count);
							} else {
								// Unregistered item, might still be in inventory though.
								for (int i = 0; i < _mod.getPlayer().getInventory().size(); ++i) {
									ItemStack stack = _mod.getPlayer().getInventory().getStack(i);
									if (!stack.isEmpty()) {
										String name = ItemHelper.stripItemName(stack.getItem());
										if (name.equals(item)) {
											target = new ItemTarget(stack.getItem(), count);
											break;
										}
									}
								}
							}
							if (target != null) {
								Debug.logMessage("USER: " + username + " : ITEM: " + item + " x " + count);
								_mod.runUserTask(new GiveItemToPlayerTask(username, target), this::finish);
							} else {
								_mod.log("Item not found or task does not exist for item: " + item);
								finish();
							}
							return SINGLE_SUCCESS;
						}))));
	}

}