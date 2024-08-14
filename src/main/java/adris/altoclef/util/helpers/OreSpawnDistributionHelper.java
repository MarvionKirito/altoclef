package adris.altoclef.util.helpers;

import static java.util.Map.entry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class OreSpawnDistributionHelper {

    protected static final Map<List<Block>, OreSpawnDistribution> ORE_SPAWN_DISTRIBUTIONS = Map.ofEntries(
	    entry(List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE), new OreSpawnDistribution(0, 96, 192)),
	    entry(List.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK), new OreSpawnDistribution(-16, 48, 112)),
	    entry(List.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK), new OreSpawnDistribution(-32, 16, 72)),
	    entry(List.of(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE), new OreSpawnDistribution(-59, 0, 64)),
	    entry(List.of(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.RAW_GOLD_BLOCK), new OreSpawnDistribution(-59, -16, 32)),
	    entry(List.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE), new OreSpawnDistribution(-59, -59, 15)),
	    entry(List.of(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE), new OreSpawnDistribution(-59, -59, 15)));

    public static Optional<OreSpawnDistribution> getOreDistributionFor(Block[] blocks) {
	Optional<OreSpawnDistribution> currentTargrtOreDistance = null;
	for (Block block : blocks) {
	    currentTargrtOreDistance = getOreDistributionFor(block);
	    if (currentTargrtOreDistance.isPresent()) return currentTargrtOreDistance;
	}
	return Optional.empty();
    }
    
    public static Optional<OreSpawnDistribution> getOreDistributionFor(List<Block> blocks) {
	Optional<OreSpawnDistribution> currentTargrtOreDistance = null;
	for (Block block : blocks) {
	    currentTargrtOreDistance = getOreDistributionFor(block);
	    if (currentTargrtOreDistance.isPresent()) return currentTargrtOreDistance;
	}
	return Optional.empty();
    }
    
    public static Optional<OreSpawnDistribution> getOreDistributionFor(Block block) {
	for (List<Block> blocks : ORE_SPAWN_DISTRIBUTIONS.keySet()) {
	    if (blocks.contains(block))
		return Optional.of(ORE_SPAWN_DISTRIBUTIONS.get(blocks));
	}

	return Optional.empty();
    }

    public static class OreSpawnDistribution {
	private final int maxHeight;
	private final int optimalHeight;
	private final int minHeight;

	OreSpawnDistribution(int minHeight, int optimalHeight, int maxHeight) {
	    this.minHeight = minHeight;
	    this.optimalHeight = optimalHeight;
	    this.maxHeight = maxHeight;
	}

	public int getMinHeight() {
	    return this.minHeight;
	}

	public int getOptimalHeight() {
	    return this.optimalHeight;
	}

	public int getMaxHeight() {
	    return this.maxHeight;
	}
    }
}
