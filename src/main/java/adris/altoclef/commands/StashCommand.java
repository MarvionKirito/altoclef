package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.arguments.ItemListArgumentType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ItemList;
import adris.altoclef.tasks.container.StoreInStashTask;
import adris.altoclef.util.BlockRange;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.util.math.BlockPos;

public class StashCommand extends Command {
    public StashCommand(AltoClef mod) throws CommandException {
        // stash <stash_x> <stash_y> <stash_z> <stash_radius> [item list]
        super("stash", "Store an item in a chest/container stash. Will deposit ALL non-equipped items if item list is empty.", mod);
    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("startPos", Vec3ArgumentType.vec3())
				.then(argument("endPos", Vec3ArgumentType.vec3())
				.then(argument("items", new ItemListArgumentType(REGISTRY_ACCESS))
						.executes(context -> {
							BlockPos startPos = context.getArgument("startPos", PosArgument.class).toAbsoluteBlockPos(MinecraftClient.getInstance().player.getCommandSource());
							BlockPos endPos = context.getArgument("endPos", PosArgument.class).toAbsoluteBlockPos(MinecraftClient.getInstance().player.getCommandSource());
							ItemList itemList = ItemListArgumentType.get(context);
							ItemTarget[] items = itemList.items;

							_mod.runUserTask(new StoreInStashTask(true, new BlockRange(startPos, endPos, WorldHelper.getCurrentDimension()), items), this::finish);
							return SINGLE_SUCCESS;
						}))));
		
		
		builder.then(argument("startPos", Vec3ArgumentType.vec3())
				.then(argument("endPos", Vec3ArgumentType.vec3())
				.executes(context -> {
					BlockPos startPos = context.getArgument("startPos", PosArgument.class).toAbsoluteBlockPos(MinecraftClient.getInstance().player.getCommandSource());
					BlockPos endPos = context.getArgument("endPos", PosArgument.class).toAbsoluteBlockPos(MinecraftClient.getInstance().player.getCommandSource());
					ItemTarget[] items = DepositCommand.getAllNonEquippedOrToolItemsAsTarget(_mod);


					_mod.runUserTask(new StoreInStashTask(true, new BlockRange(startPos, endPos, WorldHelper.getCurrentDimension()), items), this::finish);
					
					return SINGLE_SUCCESS;
				})));
		
	}
}
