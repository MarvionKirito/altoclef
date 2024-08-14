package adris.altoclef.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.arguments.ItemListArgumentType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.construction.BranchMiningTask;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.OreSpawnDistributionHelper;
import adris.altoclef.util.helpers.OreSpawnDistributionHelper.OreSpawnDistribution;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;

public class BranchMineCommand extends Command {

    private static final Map<String, Block[]> _dropToOre = new HashMap<>() {
	{
	    put("coal", new Block[] { Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE });
	    put("raw_iron", new Block[] { Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK });
	    put("raw_gold", new Block[] { Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.RAW_GOLD_BLOCK });
	    put("raw_copper", new Block[] { Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK });
	    put("diamond", new Block[] { Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE });
	    put("emerald", new Block[] { Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE });
	    put("redstone", new Block[] { Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE });
	    put("lapis_lazuli", new Block[] { Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE });
	}
    };

    public BranchMineCommand(AltoClef mod) throws CommandException {
	super("branchMine", "Create a branch mine from the current position in direction bot is currently looking at",
		mod);
    }

    private static void OnResourceDoesNotExist(AltoClef mod, String resource) {
	mod.log("\"" + resource + "\" is not a catalogued ores. Can't get it yet, sorry!", MessagePriority.OPTIONAL);
	mod.log("List of available items: ", MessagePriority.OPTIONAL);
	for (String key : _dropToOre.keySet()) {

	    mod.log("	\"" + key + "\"", MessagePriority.OPTIONAL);
	}
    }

    private void GetItems(AltoClef mod, ItemTarget... items) {
	BranchMiningTask targetTask;
	List<Block> blocksToMine = new ArrayList<>();
	if (items == null || items.length == 0) {
	    mod.log("At least one item must be specified.");
	    finish();
	    return;
	}
	for (ItemTarget itemTarget : items) {
	    if (!_dropToOre.containsKey(itemTarget.getCatalogueName())) {
		mod.log("Unexpected value: " + itemTarget.getCatalogueName() + ", expacted any of: "
			+ _dropToOre.keySet(), MessagePriority.OPTIONAL);
		finish();
		return;
	    }
	    blocksToMine.addAll(Arrays.asList(_dropToOre.get(itemTarget.getCatalogueName())));
	}
	Optional<OreSpawnDistribution> currentTargrtOreDistance = OreSpawnDistributionHelper
		.getOreDistributionFor(blocksToMine);
	if (currentTargrtOreDistance.isEmpty()) {
	    mod.log("Expacted any of: " + _dropToOre.keySet(),
		    MessagePriority.OPTIONAL);
	    return;
	}
	BlockPos homePos = new BlockPos(mod.getPlayer().getBlockX(), currentTargrtOreDistance.get().getOptimalHeight(),
		mod.getPlayer().getBlockZ());
	targetTask = new BranchMiningTask(homePos, mod.getPlayer().getMovementDirection(), blocksToMine);
	if (targetTask != null) {
	    mod.runUserTask(targetTask, this::finish);
	} else {
	    finish();
	}
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
	builder.then(
		argument("items", new ItemListArgumentType(REGISTRY_ACCESS, (item) -> _dropToOre.containsKey(item)))
			.executes(context -> {
			    GetItems(_mod, ItemListArgumentType.get(context).items);
			    return SINGLE_SUCCESS;
			}));

    }

}
