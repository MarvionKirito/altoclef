package adris.altoclef.tasks.construction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.ItemEntity;

/*
 * Mining in a following structure:
                           X = Tunnel "Trunk"
                           B = Branch
                           S = Staircase
                           Y = Outpost (for supplies, etc.)
                           - = (Optional) 1×1 block tunnel
		                           
		          B-------B                 B-------B
		          B       B                 B       B
		          B       B                 B       B
		          B       B                 B       B
		          B       B                 B       B
		          B       B                 B       B
		          B       B       YYY       B       B
		X X X X X X X X X X X X X YYY X X X X X X X X X X X X
		          B       B       YYY       B       B
		          B       B        S        B       B
		          B       B        S        B       B
		          B       B        S        B       B
		          B       B        S        B       B
		          B-------B        S        B-------B
 */

public class BranchMiningTask extends Task implements ITaskRequiresGrounded {
	
	private final BlockPos _startPos;
	private BlockPos _checkpointPos = null;
	private final Direction _startingDirection;
	private TunnelToMine _prevTunnel = null;
    private final MovementProgressChecker _progressChecker = new MovementProgressChecker();
	private final Task _prepareForMiningTask = TaskCatalogue
			.getItemTask(new ItemTarget(Items.IRON_PICKAXE, 3));
	private int _groundHeight = Integer.MIN_VALUE;
	private GetToYTask _getToYTask = null;
	private DestroyBlockTask _destroyOreTask = null;
	private GetToBlockTask _getCloseToBlockTask = null;
	private final List<Block> _blockTargets;
	private List<BlockPos> _blocksToMine;
	private Block _currentTarget;
    
    public BranchMiningTask(BlockPos homePos, Direction startingDirection, List<Block> blocksToMine) {
		if(!Arrays.asList(ItemHelper.ORES).containsAll(blocksToMine))
			throw new IllegalStateException("Unexpected value: " + blocksToMine.toString() + ", expacted any of: " + ItemHelper.ORES.toString());
		_startPos = homePos;
		_startingDirection = startingDirection;
		_blockTargets = blocksToMine;
    }

	public BranchMiningTask(BlockPos homePos, Direction startingDirection, Block blockToMine) {
		if(!Arrays.asList(ItemHelper.ORES).contains(blockToMine))
			throw new IllegalStateException("Unexpected value: " + blockToMine + ", expacted any of: " + ItemHelper.ORES.toString());
		_startPos = homePos;
		_startingDirection = startingDirection;
		List<Block> blockList = new ArrayList<>();
		blockList.add(blockToMine);
		_blockTargets = blockList;
	}

	@Override
	protected void onStart(AltoClef mod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Task onTick(AltoClef mod) {
		if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
        	_progressChecker.reset();
        }
		if(mod.getClientBaritone().getBuilderProcess().isActive())
		{
			return null;
		}
		if(_currentTarget != null)
		{
			Optional<ItemEntity> itemEntity = mod.getEntityTracker().getClosestItemDrop(ItemHelper.oreToDrop(_currentTarget.asItem()));
			if(itemEntity.isPresent()) {
				setDebugState("Picking up the drop!");
				// Ensure our inventory is free if we're close
		        boolean touching = mod.getEntityTracker().isCollidingWithPlayer(itemEntity.get());
		        if (touching) {
	                if (mod.getItemStorage().getSlotsThatCanFitInPlayerInventory(itemEntity.get().getStack(), false).isEmpty()) {
	                    return new EnsureFreeInventorySlotTask();
	                }
		        } else {
					setDebugState("Getting closer to the drop!");
		        	_getCloseToBlockTask = new GetToBlockTask(itemEntity.get().getBlockPos());
		        }
			}
		}
		
		if (_getCloseToBlockTask != null && _destroyOreTask != null && !_destroyOreTask.isActive() && !_getCloseToBlockTask.isFinished(mod))
		{
			return _getCloseToBlockTask;
		} else if(_destroyOreTask != null && _destroyOreTask.isActive() && !_destroyOreTask.isFinished(mod))
		{
			setDebugState("Mining ores found!");
			return _destroyOreTask;
		}
		else if(_blocksToMine != null && !_blocksToMine.isEmpty())
		{
			do {
				BlockPos blockPos = _blocksToMine.get(0);
				if(!WorldHelper.isAir(mod, blockPos))
				{
					_currentTarget = mod.getWorld().getBlockState(blockPos).getBlock();
					if(Math.sqrt(mod.getPlayer().getBlockPos().getSquaredDistance(blockPos)) > 5)
					{
						setDebugState("Getting closer to ore found!");
						_getCloseToBlockTask = new GetToBlockTask(blockPos);
						return _getCloseToBlockTask;
					}
					_destroyOreTask = new DestroyBlockTask(blockPos);
					_blocksToMine.remove(blockPos);
					setDebugState("Mining ores found!");
					return _destroyOreTask;
				}
				_blocksToMine.remove(blockPos);
			} while (!_blocksToMine.isEmpty());
		}
		if(_prepareForMiningTask.isActive() && !_prepareForMiningTask.isFinished(mod)
			|| isNewPickaxeRequired(mod))
		{
			if(!_progressChecker.check(mod))
			{
				if(mod.getPlayer().getBlockY() > _groundHeight)
				{
					_progressChecker.reset();
				}
				if(_groundHeight == Integer.MIN_VALUE)
				{
					_groundHeight = WorldHelper.getGroundHeight(mod, mod.getPlayer().getBlockX(), mod.getPlayer().getBlockZ());
					_getToYTask = new GetToYTask(_groundHeight + 4);
				}
				if(!(_getToYTask.isActive() || !_getToYTask.isFinished(mod)))
				{
					_groundHeight = Integer.MIN_VALUE;
					_getToYTask = null;
				}
				return _getToYTask;
			}
			if(mod.getClientBaritone().getBuilderProcess().isActive())
			{
				mod.getClientBaritone().getBuilderProcess().onLostControl();
			}
			return _prepareForMiningTask;
		}
		if (!mod.getClientBaritone().getBuilderProcess().isActive()) {
			TunnelToMine tunnel = null;
			_getCloseToBlockTask = null;
			

			if(_prevTunnel != null && wasCleared(mod, _prevTunnel))
			{
				_blocksToMine = getBlocksNextToTunnel(mod, _prevTunnel);
			}
			
			if(_checkpointPos != null)
			{
				BlockPos prevCheckpoint = _checkpointPos;
				
				tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 3, _startingDirection);
				if(_prevTunnel != null && wasCleared(mod, _prevTunnel) && wasCleared(mod, tunnel))
				{
					switch (_startingDirection) {
				        case EAST:
				        case WEST:
				        	if(_prevTunnel == null || _prevTunnel.tunnelDirection == Direction.SOUTH)
				        		tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 32, Direction.NORTH);
				        	else
				        	{
				        		tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 32, Direction.SOUTH);
				        	}
			        		break;
				        case NORTH:
				        case SOUTH:
				        	if(_prevTunnel == null || _prevTunnel.tunnelDirection == Direction.EAST)
				        		tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 32, Direction.WEST);
				        	else
				        	{
				        		tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 32, Direction.EAST);
			        	}
				        	break;
				        default:
				            throw new IllegalStateException("Unexpected value: " + _startingDirection);
					}
					if (_prevTunnel != null) setDebugState("Mining " + tunnel.tunnelDirection + " branch!");
					
					if(wasCleared(mod, tunnel))
					{
						switch (_startingDirection) {
					        case EAST:
					        	_checkpointPos = prevCheckpoint.east(4);
					        	break;
					        case WEST:
					        	_checkpointPos = prevCheckpoint.west(4);
					        	break;
					        case NORTH:
					        	_checkpointPos = prevCheckpoint.north(4);
					        	break;
					        case SOUTH:
					        	_checkpointPos = prevCheckpoint.south(4);
					        	break;
					        default:
					            throw new IllegalStateException("Unexpected value: " + _startingDirection);
						}
					}
					
					_prevTunnel = tunnel;
				} else {
					setDebugState("Mining main tunnel");
					tunnel = new TunnelToMine(mod, prevCheckpoint, 2, 1, 3, _startingDirection);
					_prevTunnel = tunnel;
				}
			} else {
				switch (_startingDirection) {
			        case EAST:
			        	_checkpointPos = _startPos.east(4);
			        	break;
			        case WEST:
			        	_checkpointPos = _startPos.west(4);
			        	break;
			        case NORTH:
			        	_checkpointPos = _startPos.north(4);
			        	break;
			        case SOUTH:
			        	_checkpointPos = _startPos.south(4);
			        	break;
			        default:
			            throw new IllegalStateException("Unexpected value: " + _startingDirection);
				}
				setDebugState("Mining main tunnel");
				tunnel = new TunnelToMine(mod, _startPos, 2, 1, 5, _startingDirection);
				_prevTunnel = tunnel;
			}
			if(tunnel != null )
			{
				mod.getClientBaritone().getBuilderProcess().clearArea(tunnel.corner1, tunnel.corner2);
			}
		} else if(!_progressChecker.check(mod))
		{
			_progressChecker.reset();
			 mod.getBehaviour().setBlockBreakAdditionalPenalty(3.0);
            mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.value = 3.0;
            mod.getBehaviour().setBlockPlacePenalty(0.0);
            mod.getClientBaritoneSettings().blockPlacementPenalty.value = 0.0;
            mod.getClientBaritoneSettings().costHeuristic.value = 5.5;
		}
		return null;
	}
	
	protected static boolean isNewPickaxeRequired(AltoClef mod)
    {

		Item[] PICKAXES = new Item[] { 
    			Items.STONE_PICKAXE,
    			Items.IRON_PICKAXE,
    			Items.DIAMOND_PICKAXE,
    			Items.NETHERITE_PICKAXE,
    			Items.GOLDEN_PICKAXE};
    	
    	if(mod.getItemStorage().getSlotsWithItemScreen(
    			Items.WOODEN_PICKAXE, 
    			Items.STONE_PICKAXE,
    			Items.IRON_PICKAXE,
    			Items.DIAMOND_PICKAXE,
    			Items.NETHERITE_PICKAXE,
    			Items.GOLDEN_PICKAXE
			).size() == 0)
    	{
    		return true;
    	}

    	for (Item pickaxe : PICKAXES)
    	{
    		if(mod.getItemStorage().getSlotsWithItemScreen(pickaxe).size() > 0)
    		{
				for(Slot slot : mod.getItemStorage().getSlotsWithItemScreen(pickaxe))
				{
					if(
						mod.getItemStorage().getItemStacksPlayerInventory(false).get(
								slot.getInventorySlot()
						).getDamage() < (pickaxe.getDefaultStack().getMaxDamage() * 0.4)
					) {
		    			return false;
					}
				}
			}
    	}
    	return true;
    }
    
	
	private boolean wasCleared(AltoClef mod,TunnelToMine tunnel) { 
		int x1 = tunnel.corner1.getX();
	    int y1 = tunnel.corner1.getY();
	    int z1 = tunnel.corner1.getZ();
	
	    int x2 = tunnel.corner2.getX();
	    int y2 = tunnel.corner2.getY();
	    int z2 = tunnel.corner2.getZ();
	
	    // Swap coordinates if necessary to make sure x1 <= x2, y1 <= y2, and z1 <= z2
	    if (x1 > x2) {
	        int temp = x1;
	        x1 = x2;
	        x2 = temp;
	    }
	    if (y1 > y2) {
	        int temp = y1;
	        y1 = y2;
	        y2 = temp;
	    }
	    if (z1 > z2) {
	        int temp = z1;
	        z1 = z2;
	        z2 = temp;
	    }
	
	    // Check each block between pos1 and pos2 for air
	    for (int x = x1; x <= x2; x++) {
	        for (int y = y1; y <= y2; y++) {
	            for (int z = z1; z <= z2; z++) {
//                	BlockState state = MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z));
    				if(!WorldHelper.isAir(mod, new BlockPos(x, y, z)))
                	{
                		return false;
                	}
                }
            }
        }
		
		return true;
	}

	private List<BlockPos> getBlocksNextToTunnel(AltoClef mod,TunnelToMine tunnel) {
		int x1 = tunnel.corner1.getX();
	    int y1 = tunnel.corner1.getY();
	    int z1 = tunnel.corner1.getZ();
	
	    int x2 = tunnel.corner2.getX();
	    int y2 = tunnel.corner2.getY();
	    int z2 = tunnel.corner2.getZ();
	    List<BlockPos> vain = new ArrayList<BlockPos>();
	    // Swap coordinates if necessary to make sure x1 <= x2, y1 <= y2, and z1 <= z2
	    if (x1 > x2) {
	        int temp = x1;
	        x1 = x2;
	        x2 = temp;
	    }
	    if (y1 > y2) {
	        int temp = y1;
	        y1 = y2;
	        y2 = temp;
	    }
	    if (z1 > z2) {
	        int temp = z1;
	        z1 = z2;
	        z2 = temp;
	    }
	
	    // Check each block between pos1 and pos2 for air
	    for (int x = x1; x <= x2; x++) {
	        for (int y = y1; y <= y2; y++) {
	            for (int z = z1; z <= z2; z++) {
	            	BlockPos currentBlock = new BlockPos(x, y, z);
    				for (Direction dir : Direction.values()) {
						BlockPos blockPosToCheck = currentBlock.offset(dir);
						Block blockToCheck = mod.getWorld().getBlockState(blockPosToCheck).getBlock();
						if(
								_blockTargets.contains(blockToCheck) 
								&& !vain.contains(blockPosToCheck)
								&& !mod.getBlockTracker().unreachable(blockPosToCheck)
								&& WorldHelper.canBreak(mod, blockPosToCheck)
						)
						{
							vain.add(blockPosToCheck);
							vain.addAll(findAdjacentBlocksOfSameType(mod, blockPosToCheck, blockToCheck, vain));
						}
					}
                }
            }
        }
	    
		return vain;
	}
	
	private List<BlockPos> findAdjacentBlocksOfSameType(AltoClef mod, BlockPos startPos, Block targetBlock, List<BlockPos> currVain) {
		List<BlockPos> connectedBlocks = new ArrayList<BlockPos>();
        Set<BlockPos> visited = new HashSet<BlockPos>();
        Queue<BlockPos> queue = new LinkedList<BlockPos>();
        
        BlockState targetBlockState = mod.getWorld().getBlockState(startPos);
        
        queue.add(startPos);
        visited.add(startPos);
        queue.addAll(currVain);
        visited.addAll(currVain);
        
        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            connectedBlocks.add(currentPos);

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.offset(direction);
                if (visited.contains(neighborPos)) {
                    continue; // Skip already visited positions
                }

                BlockState neighborBlockState = mod.getWorld().getBlockState(neighborPos);
                if (neighborBlockState.getBlock() == targetBlockState.getBlock()
						&& !mod.getBlockTracker().unreachable(neighborPos)
						&& WorldHelper.canBreak(mod, neighborPos)
                		) {
                    queue.add(neighborPos);
                    visited.add(neighborPos);
                }
            }
        }

        return connectedBlocks;
	}
	
	
	@Override
	protected void onStop(AltoClef mod, Task interruptTask) {
		mod.getClientBaritone().getBuilderProcess().onLostControl();
		
	}

	@Override
	protected boolean isEqual(Task other) {
		if (other instanceof BranchMiningTask) {
			BranchMiningTask task = (BranchMiningTask) other;
            return (task._startPos.equals(_startPos));
        }
        return false;
	}

	@Override
	protected String toDebugString() {
		return "Branch mining! Y=" + _startPos.getY();
	}

}

class TunnelToMine {
    public final BlockPos corner1;
    public final BlockPos corner2;
    public final Direction tunnelDirection;
    
    public TunnelToMine(AltoClef mod, BlockPos startPos, int height, int width, int depth, Direction enumFacing) {
    	height--;
        width--;
        tunnelDirection = enumFacing;
	    int addition = ((width % 2 == 0) ? 0 : 1);
	    switch (enumFacing) {
	        case EAST:
	            corner1 = new BlockPos(startPos.getX(), startPos.getY(), startPos.getZ() - width / 2);
//	            corner2 = new BlockPos(startPos.getX() + depth, startPos.getY() + height, startPos.getZ() + width / 2 + addition);
	            corner2 = getSafeTunnelCorner2(
		            		mod, corner1, 
		            		new BlockPos(startPos.getX() + depth, startPos.getY() + height, startPos.getZ() + width / 2 + addition),
		            		enumFacing
	            		);
	            break;
	        case WEST:
	            corner1 = new BlockPos(startPos.getX(), startPos.getY(), startPos.getZ() + width / 2 + addition);
//	            corner2 = new BlockPos(startPos.getX() - depth, startPos.getY() + height, startPos.getZ() - width / 2);
	            corner2 = getSafeTunnelCorner2(
	            		mod, corner1, 
	            		new BlockPos(startPos.getX() - depth, startPos.getY() + height, startPos.getZ() - width / 2),
	            		enumFacing
            		);
	            break;
	        case NORTH:
	            corner1 = new BlockPos(startPos.getX() - width / 2, startPos.getY(), startPos.getZ());
//	            corner2 = new BlockPos(startPos.getX() + width / 2 + addition, startPos.getY() + height, startPos.getZ() - depth);
	            corner2 = getSafeTunnelCorner2(
	            		mod, corner1, 
	            		new BlockPos(startPos.getX() + width / 2 + addition, startPos.getY() + height, startPos.getZ() - depth),
	            		enumFacing
            		);
	            break;
	        case SOUTH:
	            corner1 = new BlockPos(startPos.getX() + width / 2 + addition, startPos.getY(), startPos.getZ());
//	            corner2 = new BlockPos(startPos.getX() - width / 2, startPos.getY() + height, startPos.getZ() + depth);
	            corner2 = getSafeTunnelCorner2(
	            		mod, corner1, 
	            		new BlockPos(startPos.getX() - width / 2, startPos.getY() + height, startPos.getZ() + depth),
	            		enumFacing
            		);
	            break;
	        default:
	            throw new IllegalStateException("Unexpected value: " + enumFacing);
	    }
    }
    
    private static BlockPos getSafeTunnelCorner2(AltoClef mod, BlockPos corner1, BlockPos corner2, Direction direction) {
    	int x1 = corner1.getX();
	    int y1 = corner1.getY();
	    int z1 = corner1.getZ();
	
	    int x2 = corner2.getX();
	    int y2 = corner2.getY();
	    int z2 = corner2.getZ();

	    double closestDistance = Double.MAX_VALUE;
	    BlockPos closest = corner2;
	    
	    // Swap coordinates if necessary to make sure x1 <= x2, y1 <= y2, and z1 <= z2
	    if (x1 > x2) {
	        int temp = x1;
	        x1 = x2;
	        x2 = temp;
	    }
	    if (y1 > y2) {
	        int temp = y1;
	        y1 = y2;
	        y2 = temp;
	    }
	    if (z1 > z2) {
	        int temp = z1;
	        z1 = z2;
	        z2 = temp;
	    }
	    // Check each block between pos1 and pos2
	    for (int x = x1; x <= x2; x++) {
	        for (int y = y1; y <= y2; y++) {
	            for (int z = z1; z <= z2; z++) {
	            	BlockPos pos = new BlockPos(x, y, z);
            		for (Direction neighborDirection : Direction.values()) {
            	        BlockPos neighborPos = pos.offset(neighborDirection);
            	        if (mod.getWorld().getBlockState(neighborPos).getBlock() instanceof FluidBlock) {
            	        	double distance = pos.getSquaredDistance(corner1);
                            if (distance < closestDistance) {
                                closest = pos.offset(direction.getOpposite());
                                closestDistance = distance;
                            }
            	        } else if (mod.getWorld().getBlockState(neighborPos).getBlock() instanceof FallingBlock) {
            	        	for (int i = 0; i < mod.getWorld().getHeight(); i++)
							{
            	        		if(mod.getWorld().getBlockState(neighborPos).getBlock() instanceof FallingBlock) continue;
            	        		if(!(mod.getWorld().getBlockState(neighborPos.up(i)).getBlock() instanceof FluidBlock)) break;
								if(mod.getWorld().getBlockState(neighborPos.up(i)).getBlock() instanceof FluidBlock) {
		            	        	double distance = pos.getSquaredDistance(corner1);
		                            if (distance < closestDistance) {
		                                closest = pos.offset(direction.getOpposite());
		                                closestDistance = distance;
		                            }
		                            break;
								}
							}
            	        }
            	    }
                }
            }
        }
	    
    	return closest;
    }
}

