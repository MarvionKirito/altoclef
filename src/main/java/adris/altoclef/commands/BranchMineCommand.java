package adris.altoclef.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commands.arguments.ItemListArgumentType;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ItemList;
import adris.altoclef.tasks.construction.BranchMiningTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class BranchMineCommand extends Command {
	
	private static final Map<String, Block[]> _dropToOre = new HashMap<>() {
    	{
    		put("coal", new Block[]{Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE});
    		put("raw_iron", new Block[]{Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE});
    		put("raw_gold", new Block[]{Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE});
    		put("raw_copper", new Block[]{Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE});
    		put("diamond", new Block[]{Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE});
    		put("emerald", new Block[]{Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE});
    		put("redstone", new Block[]{Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE});
    		put("lapis_lazuli", new Block[]{Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE});
    	}
    };

	public BranchMineCommand(AltoClef mod) throws CommandException {
        super("branchMine", "Create a branch mine from the current position in direction bot is currently looking at", mod);
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
            mod.log("You must specify at least one item!");
            finish();
            return;
        }
        for (ItemTarget itemTarget : items)
		{
			if(!_dropToOre.containsKey(itemTarget.getCatalogueName()))
			{
				mod.log("Unexpected value: " + itemTarget.getCatalogueName() + ", expacted any of: " + _dropToOre.keySet(), MessagePriority.OPTIONAL);
		        finish();
		        return;
			}
			blocksToMine.addAll(Arrays.asList(_dropToOre.get(itemTarget.getCatalogueName())));
		}
        OreDistribution currOreDis = new OreDistribution(blocksToMine);
        BlockPos homePos = new BlockPos(mod.getPlayer().getBlockX(), currOreDis.optimalHeight, mod.getPlayer().getBlockZ());
        targetTask = new BranchMiningTask(
    		homePos, 
			mod.getPlayer().getMovementDirection(),
			blocksToMine
		);
        if (targetTask != null) {
            mod.runUserTask(targetTask, this::finish);
        } else {
            finish();
        }
    }
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.then(argument("items", new ItemListArgumentType(REGISTRY_ACCESS, (item) -> _dropToOre.containsKey(item))).executes(context -> {
	        GetItems(_mod, ItemListArgumentType.get(context).items);
	        return SINGLE_SUCCESS;
		}));
		
	}
	
	
	class OreDistribution {
		
		public final int maxHeight;
		public final int optimalHeight;
		public final int minHeight;
		
		OreDistribution(List<Block> blocks)
		{
			int _maxHeight = Integer.MIN_VALUE;
	        int _minHeight = Integer.MAX_VALUE;
	        int _maxOptimalHeight = Integer.MIN_VALUE;
	        int _minOptimalHeight = Integer.MAX_VALUE;
			List<OreDistribution> oreDistributions = new ArrayList<BranchMineCommand.OreDistribution>();
			for (Block block : blocks)
			{
				oreDistributions.add(new OreDistribution(block));
			}
			
			for (OreDistribution oreDistribution : oreDistributions) {
	            int optimalHeight = oreDistribution.optimalHeight;
	            _maxOptimalHeight = Math.max(_maxOptimalHeight, optimalHeight);
	            _minOptimalHeight = Math.min(_minOptimalHeight, optimalHeight);
	            _maxHeight = Math.max(_maxHeight, oreDistribution.maxHeight);
	            _minHeight = Math.min(_minHeight, oreDistribution.minHeight);
	        }
			
			maxHeight = _maxHeight;
			optimalHeight = (_maxOptimalHeight + _minOptimalHeight) / 2;
			minHeight = _minHeight;
			
		}
		
		OreDistribution(Block block)
		{

	    	if(block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)
	    	{
	    		maxHeight = 192;
	    		optimalHeight = 96;
	    		minHeight = 0;
	    	}else
	    	if(block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)
	    	{
	    		maxHeight = 112;
	    		optimalHeight = 48;
	    		minHeight = -16;
	    	}else
	    	if(block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)
	    	{
	    		maxHeight = 72;
	    		optimalHeight = 16;
	    		minHeight = -32;
	    	}else
	    	if(block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)
	    	{
	    		maxHeight = 64;
	    		optimalHeight = 0;
	    		minHeight = -59;
	    	}else
	    	if(block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE)
	    	{
	    		maxHeight = 32;
	    		optimalHeight = -16;
	    		minHeight = -59;
	    	}else
	    	if(block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)
	    	{
	    		maxHeight = 15;
	    		optimalHeight = -59;
	    		minHeight = -59;
	    	}else
	        if(block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)
	        {
	    		maxHeight = 15;
	    		optimalHeight = -59;
	    		minHeight = -59;
			}else
			{
		    	maxHeight = 8;
				optimalHeight = 8;
				minHeight = 8;
			}
//	    	throw new IllegalArgumentException("Unexpected value: " + block);
		}
	}



}
