package adris.altoclef.tasks.construction;

import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ProjectileProtectionWallTask extends Task implements ITaskRequiresGrounded {

	private BlockPos _targetPlacePos;
	private TimerGame _waitForBlockPlacement = new TimerGame(2);
	
	private static AltoClef _mod;
	
	public ProjectileProtectionWallTask(AltoClef mod) {
		_mod = mod;
	}
	
	@Override
	protected void onStart(AltoClef mod) {
		// TODO Auto-generated method stub
		_waitForBlockPlacement.forceElapse();
	}

	@Override
	protected Task onTick(AltoClef mod) {
		// TODO Auto-generated method stub
		
		if (_targetPlacePos != null && !WorldHelper.isSolid(mod, _targetPlacePos)) {
			Optional<adris.altoclef.util.slots.Slot> slot = StorageHelper.getSlotWithThrowawayBlock(_mod, true);
			if(slot.isPresent()) {
				place(_targetPlacePos, Hand.MAIN_HAND, slot.get().getInventorySlot());
				_targetPlacePos = null;
				setDebugState(null);
			}
			return null;
		}
		
		Optional<Entity> sentity = mod.getEntityTracker().getClosestEntity((e) -> {
        	if(e instanceof SkeletonEntity 
        			&& EntityHelper.isAngryAtPlayer(mod, e)
        			&& 
        			(((SkeletonEntity) e).getItemUseTime() > 8)
        			) return true;
        	return false;
        }, SkeletonEntity.class);
        if(sentity.isPresent()) {
    		Vec3d playerPos = mod.getPlayer().getPos();
            Vec3d targetPos = sentity.get().getPos();
    		// Calculate the direction vector towards the target entity
            Vec3d direction = playerPos.subtract(targetPos).normalize();

            // Calculate the new position two blocks away in the direction of the entity
            double x = playerPos.x - 2 * direction.x;
            double y = playerPos.y + direction.y;
            double z = playerPos.z - 2 * direction.z;
            
            _targetPlacePos = new BlockPos((int) x, (int) y+1, (int) z);
			setDebugState("Placing at " + _targetPlacePos.toString());
			_waitForBlockPlacement.reset();
        }
		return null;
	}

	@Override
	protected void onStop(AltoClef mod, Task interruptTask) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public boolean isFinished(AltoClef mod) {
        assert MinecraftClient.getInstance().world != null;
        
        Optional<Entity> entity = mod.getEntityTracker().getClosestEntity((e) -> {
        	if(e instanceof SkeletonEntity 
        			&& EntityHelper.isAngryAtPlayer(mod, e)
        			&& 
        			(((SkeletonEntity) e).getItemUseTime() > 3)
        			) return true;
        	return false;
        }, SkeletonEntity.class);
        
        return _targetPlacePos != null && WorldHelper.isSolid(mod, _targetPlacePos) || entity.isEmpty();
    }

	@Override
	protected boolean isEqual(Task other) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected String toDebugString() {
		// TODO Auto-generated method stub
		return "Placing blocks to block projectiles";
	}
	
	public static Direction getPlaceSide(BlockPos blockPos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = _mod.getWorld().getBlockState(neighbor);

            // Check if neighbour isn't empty
            if (state.isAir() || isClickable(state.getBlock())) continue;

            // Check if neighbour is a fluid
            if (!state.getFluidState().isEmpty()) continue;

            return side;
        }

        return null;
    }
	
	public static boolean place(BlockPos blockPos, Hand hand, int slot) {
        if (slot < 0 || slot > 8) return false;
        if (!canPlace(blockPos)) return false;

        Vec3d hitPos = Vec3d.ofCenter(blockPos);

        BlockPos neighbour;
        Direction side = getPlaceSide(blockPos);

        if (side == null) {
        	place(blockPos.down(), hand, slot);
        	return false;
        } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);

        _mod.getPlayer().setYaw((float) getYaw(hitPos));
        _mod.getPlayer().setPitch((float) getPitch(hitPos));
		swap(slot);

        interact(bhr, hand);


        return true;
    }
    
	
	public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock
            || block instanceof AnvilBlock
            || block instanceof ButtonBlock
            || block instanceof AbstractPressurePlateBlock
            || block instanceof BlockWithEntity
            || block instanceof BedBlock
            || block instanceof FenceGateBlock
            || block instanceof DoorBlock
            || block instanceof NoteBlock
            || block instanceof TrapdoorBlock;
    }
	
	public static void interact(BlockHitResult blockHitResult, Hand hand) {
        boolean wasSneaking = _mod.getPlayer().input.sneaking;
        _mod.getPlayer().input.sneaking = false;

        ActionResult result = _mod.getController().interactBlock(_mod.getPlayer(), hand, blockHitResult);

        if (result.shouldSwingHand()) {
            _mod.getPlayer().swingHand(hand);
        }

        _mod.getPlayer().input.sneaking = wasSneaking;
    }

	public static boolean canPlace(BlockPos blockPos, boolean checkEntities) {
        if (blockPos == null) return false;

        // Check y level
        if (!World.isValid(blockPos)) return false;

        // Check if current block is replaceable
        if (!_mod.getWorld().getBlockState(blockPos).isReplaceable()) return false;

        // Check if intersects entities
        return !checkEntities || _mod.getWorld().canPlace(Blocks.OBSIDIAN.getDefaultState(), blockPos, ShapeContext.absent());
    }

    public static boolean canPlace(BlockPos blockPos) {
        return canPlace(blockPos, true);
    }
	
    public static boolean swap(int slot) {
        if (slot == PlayerSlot.OFFHAND_SLOT.getInventorySlot()) return true;
        if (slot < 0 || slot > 8) return false;

        _mod.getPlayer().getInventory().selectedSlot = slot;
        return true;
    }
    
    public static double getYaw(Vec3d pos) {
        return _mod.getPlayer().getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() - _mod.getPlayer().getZ(), pos.getX() - _mod.getPlayer().getX())) - 90f - _mod.getPlayer().getYaw());
    }

    public static double getPitch(Vec3d pos) {
        double diffX = pos.getX() - _mod.getPlayer().getX();
        double diffY = pos.getY() - (_mod.getPlayer().getY() + _mod.getPlayer().getEyeHeight(_mod.getPlayer().getPose()));
        double diffZ = pos.getZ() - _mod.getPlayer().getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return _mod.getPlayer().getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - _mod.getPlayer().getPitch());
    }
}
