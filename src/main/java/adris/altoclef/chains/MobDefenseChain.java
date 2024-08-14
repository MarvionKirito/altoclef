package adris.altoclef.chains;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.control.KillAura;
import adris.altoclef.tasks.construction.ProjectileProtectionWallTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.CustomBaritoneGoalTask;
import adris.altoclef.tasks.movement.RunAwayFromCreepersTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.ProjectileHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.Baritone;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

// TODO: Check on the anti-surround system
// TODO: Optimise shielding against spiders and skeletons

public class MobDefenseChain extends SingleTaskChain
{
	private static final double DANGER_KEEP_DISTANCE = 30;
	private static final double CREEPER_KEEP_DISTANCE = 10;
	private static final double ARROW_KEEP_DISTANCE_HORIZONTAL = 2;// 4;
	private static final double ARROW_KEEP_DISTANCE_VERTICAL = 10;// 15;
	private static final double SAFE_KEEP_DISTANCE = 8;
	private static final Task _getThrouwawayBlocks = TaskCatalogue.getItemTask(Items.DIRT, 16);
	private final DragonBreathTracker _dragonBreathTracker = new DragonBreathTracker();
	private final KillAura _killAura = new KillAura();
	private final HashMap<Entity, TimerGame> _closeAnnoyingEntities = new HashMap<>();
	private final MovementProgressChecker _movementChecker = new MovementProgressChecker(6, 6.0, 0.5, 0.001, 2);
	private Entity _targetEntity;
	private static boolean _shielding = false;
	private boolean _doingFunkyStuff = false;
	private boolean _wasPuttingOutFire = false;
	private CustomBaritoneGoalTask _runAwayTask;

	private float _cachedLastPriority;

	public MobDefenseChain(TaskRunner runner)
	{
		super(runner);
	}

	public static double getCreeperSafety(Vec3d pos, CreeperEntity creeper)
	{
		double distance = creeper.squaredDistanceTo(pos);
		float fuse = creeper.getClientFuseTime(1);

		// Not fusing.
		if (fuse <= 0.001f) return distance;
		return distance * 0.2; // less is WORSE
	}

	@Override
	public float getPriority(AltoClef mod)
	{
		_cachedLastPriority = getPriorityInner(mod);
		return _cachedLastPriority;
	}

	private static void startShielding(AltoClef mod)
	{
		_shielding = true;
		mod.getClientBaritone().getPathingBehavior().requestPause();
		mod.getExtraBaritoneSettings().setInteractionPaused(true);
		if (!mod.getPlayer().isBlocking())
		{
			ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
			if (handItem.contains(DataComponentTypes.FOOD))
			{
				List<ItemStack> spaceSlots = mod.getItemStorage().getItemStacksPlayerInventory(false);
				if (!spaceSlots.isEmpty())
				{
					for (ItemStack spaceSlot : spaceSlots)
					{
						if (spaceSlot.isEmpty())
						{
							mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(), 0, SlotActionType.QUICK_MOVE);
							return;
						}
					}
				}
				Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
				garbage.ifPresent(
						slot -> mod.getSlotHandler().forceEquipItem(StorageHelper.getItemStackInSlot(slot).getItem()));
			}
		}
		mod.getInputControls().hold(Input.SNEAK);
		mod.getInputControls().hold(Input.CLICK_RIGHT);
	}

	private void stopShielding(AltoClef mod)
	{
		if (_shielding)
		{
			ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
			if (cursor.contains(DataComponentTypes.FOOD))
			{
				Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false)
						.or(() -> StorageHelper.getGarbageSlot(mod));
				if (toMoveTo.isPresent())
				{
					Slot garbageSlot = toMoveTo.get();
					mod.getSlotHandler().clickSlot(garbageSlot, 0, SlotActionType.PICKUP);
				}
			}
			mod.getInputControls().release(Input.SNEAK);
			mod.getInputControls().release(Input.CLICK_RIGHT);
			mod.getExtraBaritoneSettings().setInteractionPaused(false);
			_shielding = false;
		}
	}

	public boolean isShielding()
	{
		return _shielding || _killAura.isShielding();
	}

	private static boolean willBlockedByShield(@NotNull LivingEntity entity, @Nullable Vec3d pos)
	{
		if (entity.isBlocking())
		{
			if (pos != null)
			{
				Vec3d vec3d = entity.getRotationVec(1.0F);
				Vec3d vec3d2 = pos.relativize(entity.getPos()).normalize();
				vec3d2 = new Vec3d(vec3d2.x, 0.0, vec3d2.z);
				return vec3d2.dotProduct(vec3d) < 0.0;
			}
		}
		return false;
	}

	private static int getBlockingProcess(PlayerEntity player, Boolean ignored)
	{
		if (player.isUsingItem() && !player.getActiveItem().isEmpty())
		{
			Item activeItem = player.getActiveItem().getItem();
			if (ignored)
			{
				return 6;
			} else if (activeItem.getUseAction(player.getActiveItem()) != UseAction.BLOCK)
			{
				return 0;
			} else
			{
				return Math.min(activeItem.getMaxUseTime(player.getActiveItem(), player) - player.getItemUseTimeLeft(), 6);
			}
		}
		return 0;
	}

	private boolean escapeDragonBreath(AltoClef mod)
	{
		_dragonBreathTracker.updateBreath(mod);
		for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(mod))
		{
			if (_dragonBreathTracker.isTouchingDragonBreath(playerIn))
			{
				return true;
			}
		}
		return false;
	}

	public float getPriorityInner(AltoClef mod)
	{
		if (!AltoClef.inGame())
		{
			return Float.NEGATIVE_INFINITY;
		}

		if (!mod.getModSettings().isMobDefense())
		{
			return Float.NEGATIVE_INFINITY;
		}

		// Pause if we're not loaded into a world.
		if (!AltoClef.inGame()) return Float.NEGATIVE_INFINITY;

		if (mod.getWorld().getDifficulty() == Difficulty.PEACEFUL) return Float.NEGATIVE_INFINITY;

		// Attempt to get baritone to keep away from mobs as much as possible. Batter safe then sorry after all...
		if (WorldHelper.isVulnurable(mod) && !(_runAwayTask != null && _runAwayTask.isActive())
				|| !_movementChecker.check(mod))
		{
			// if set above 1 it's "encourage breaking" instead
			mod.getClientBaritoneSettings().avoidBreakingMultiplier.value = 2.0;
			mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.value = 0.0;
		} else
		{
			mod.getClientBaritoneSettings().avoidBreakingMultiplier.reset();
			mod.getClientBaritoneSettings().blockBreakAdditionalPenalty.reset();
		}
		// Try to STAY AWAY from spawners as they are causing A LOT of problems.
		mod.getClientBaritoneSettings().mobSpawnerAvoidanceCoefficient.value = 3200.0;
		mod.getClientBaritoneSettings().mobSpawnerAvoidanceRadius.value = WorldHelper.isVulnurable(mod) ? 24 : 16;
		if (mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD))
		{
			mod.getClientBaritoneSettings().mobAvoidanceRadius.value = 20;
			mod.getClientBaritoneSettings().mobAvoidanceCoefficient.value = 46.0;
		} else
		{
			mod.getClientBaritoneSettings().mobAvoidanceRadius.value = 20;
			mod.getClientBaritoneSettings().mobAvoidanceCoefficient.value = 60.0;
		}
		// Apply avoidance if not already running away from mobs, avoiding mobs if at
		// all possible.

		// If we are not already running away from mobs then we want to avoid
		// encountering any in the first place.
		// Costly.
		// Do not disable avoidance unless we are running from mobs. Since we dont even
		// want to encounter any mobs.
		if (mod.getClientBaritoneSettings().avoidance.value != !(_runAwayTask != null))
		{
			mod.getClientBaritoneSettings().avoidance.value = !(_runAwayTask != null);
		}

		// Put out fire if we're standing on one like an idiot
		BlockPos fireBlock = isInsideFireAndOnFire(mod);
		if (fireBlock != null)
		{
			putOutFire(mod, fireBlock);
			_wasPuttingOutFire = true;
		} else
		{
			// Stop putting stuff out if we no longer need to put out a fire.
			mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
			_wasPuttingOutFire = false;
		}

		// Run away if a weird mob is close by.
		Optional<Entity> universallyDangerous = getUniversallyDangerousMob(mod);
		if (universallyDangerous.isPresent() && mod.getPlayer().getHealth() <= 10)
		{
			_runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
			setTask(_runAwayTask);
			return 70;
		}

		_doingFunkyStuff = false;
		PlayerSlot offhandSlot = PlayerSlot.OFFHAND_SLOT;
		Item offhandItem = StorageHelper.getItemStackInSlot(offhandSlot).getItem();
		// Run away from creepers
		CreeperEntity blowingUp = getClosestFusingCreeper(mod);
		if (blowingUp != null)
		{
			if ((!mod.getFoodChain().needsToEat() || mod.getPlayer().getHealth() < 9)
					&& (mod.getItemStorage().hasItem(Items.SHIELD)
							|| mod.getItemStorage().hasItemInOffhand(Items.SHIELD))
					&& !mod.getEntityTracker().entityFound(PotionEntity.class)
					&& !mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem)
					&& mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
					&& blowingUp.getClientFuseTime(blowingUp.getFuseSpeed()) > 0.5)
			{
				LookHelper.lookAt(mod, blowingUp.getEyePos());
				ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
				if (shieldSlot.getItem() != Items.SHIELD)
				{
					mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
				} else
				{
					startShielding(mod);
				}
			} else
			{
				_doingFunkyStuff = true;
				_runAwayTask = new RunAwayFromCreepersTask(CREEPER_KEEP_DISTANCE);
				setTask(_runAwayTask);
				return 50 + blowingUp.getClientFuseTime(1) * 50;
			}
		}
		synchronized (BaritoneHelper.MINECRAFT_LOCK)
		{
			// Block projectiles with shield
			if (mod.getModSettings().isDodgeProjectiles()
					&& (mod.getItemStorage().hasItem(Items.SHIELD)
							|| mod.getItemStorage().hasItemInOffhand(Items.SHIELD))
					&& !mod.getPlayer().getItemCooldownManager().isCoolingDown(offhandItem)
					&& mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
					&& !mod.getEntityTracker().entityFound(PotionEntity.class) && isProjectileClose(mod))
			{
				ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
				if (shieldSlot.getItem() != Items.SHIELD)
				{
					mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
				} else
				{
					startShielding(mod);
				}
				return 60;
			}
			if (blowingUp == null && !isProjectileClose(mod))
			{
				stopShielding(mod);
			}
		}

		if (mod.getFoodChain().needsToEat() || mod.getMLGBucketChain().isFallingOhNo(mod)
				|| !mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isChorusFruiting())
		{
			_killAura.stopShielding(mod);
			stopShielding(mod);
			return Float.NEGATIVE_INFINITY;
		}

		synchronized (BaritoneHelper.MINECRAFT_LOCK)
		{
			// Deal with close spiders before others as they jump at the player and cave spiders give poison.
			Optional<Entity> entity = mod.getEntityTracker().getClosestEntity(e -> {
				if (e instanceof CaveSpiderEntity)
				{
					CaveSpiderEntity sEntity = (CaveSpiderEntity) e;
					if (sEntity.distanceTo(mod.getPlayer()) < 5 && EntityHelper.isAngryAtPlayer(mod, sEntity)
							&& LookHelper.seesPlayer(sEntity, mod.getPlayer(), SAFE_KEEP_DISTANCE))
						return true;
				} else if (e instanceof SpiderEntity)
				{
					SpiderEntity sEntity = (SpiderEntity) e;
					if (sEntity.isInAttackRange(mod.getPlayer()) && EntityHelper.isAngryAtPlayer(mod, sEntity)
							&& LookHelper.seesPlayer(sEntity, mod.getPlayer(), SAFE_KEEP_DISTANCE)
					) return true;
				}
				return false;
			}, SpiderEntity.class, CaveSpiderEntity.class);
			if (entity.isPresent() && !WorldHelper.isSurroundedByHostiles(mod)
					&& mod.getItemStorage().hasItem(Items.SHIELD)
					&& mod.getItemStorage().hasItemInOffhand(Items.SHIELD))
			{
				if (mod.getClientBaritone().getPathingBehavior().isSafeToCancel())
				{
					LookHelper.lookAt(mod, entity.get().getEyePos());
					startShielding(mod);
					doForceField(mod);
				}
				return 60;
			}
		}

		// Force field
		doForceField(mod);

		// Dodge projectiles
		if (mod.getPlayer().getHealth() <= 10 && (!mod.getItemStorage().hasItem(Items.SHIELD)
				&& !mod.getItemStorage().hasItemInOffhand(Items.SHIELD)))
		{
			if ((StorageHelper.getNumberOfThrowawayBlocks(mod) - 1) > 1
					&& mod.getItemStorage().getItemCount(Items.DIRT) > 1 && !mod.getFoodChain().needsToEat()
					&& mod.getModSettings().isDodgeProjectiles() && isProjectileClose(mod))
			{
				_doingFunkyStuff = true;
//              _runAwayTask = new DodgeProjectilesTask(ARROW_KEEP_DISTANCE_HORIZONTAL, ARROW_KEEP_DISTANCE_VERTICAL);
//              setTask(_runAwayTask);
				setTask(new ProjectileProtectionWallTask(mod));
				return 65;
			}
		}
		// Dodge all mobs cause we boutta die son
		if (isInDanger(mod) && !escapeDragonBreath(mod) && !mod.getFoodChain().isShouldStop())
		{
			if (_targetEntity == null || WorldHelper.isSurroundedByHostiles(mod))
			{
				mod.getClientBaritoneSettings().avoidance.value = false;
				_runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
				setTask(_runAwayTask);
				return 70;
			}
		}

		if (mod.getModSettings().shouldDealWithAnnoyingHostiles())
		{
			// Deal with hostiles because they are annoying.
			List<Entity> hostiles = mod.getEntityTracker().getHostiles();

			SwordItem bestSword = null;
			Item[] SWORDS = new Item[] {
					Items.NETHERITE_SWORD, Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.STONE_SWORD,
					Items.WOODEN_SWORD
			};
			for (Item item : SWORDS)
			{
				if (mod.getItemStorage().hasItem(item))
				{
					bestSword = (SwordItem) item;
				}
			}

			List<Entity> toDealWith = new ArrayList<>();

			if (!hostiles.isEmpty())
			{
				if (WorldHelper.isSurrounded(mod, hostiles))
				{
					mod.getClientBaritoneSettings().avoidance.value = false;
					_doingFunkyStuff = true;
					Debug.logMessage("We are surrounded. Repositioning...");
					stopShielding(mod);
					// We can't deal with this as we are getting surrounded so lets reposition...
					_runAwayTask = new RunAwayFromHostilesTask(6, true);
					setTask(_runAwayTask);
					return 80;
				}

				synchronized (BaritoneHelper.MINECRAFT_LOCK)
				{
					for (Entity hostile : hostiles)
					{
						boolean isRangedOrPoisnous = (hostile instanceof SkeletonEntity
								|| hostile instanceof WitchEntity || hostile instanceof PillagerEntity
								|| hostile instanceof PiglinEntity || hostile instanceof StrayEntity
								|| hostile instanceof CaveSpiderEntity);
						int annoyingRange = 10;
						if (isRangedOrPoisnous && !mod.getItemStorage().hasItem(Items.SHIELD)
								&& !mod.getItemStorage().hasItemInOffhand(Items.SHIELD))
							annoyingRange = 35;
						else if (isRangedOrPoisnous) annoyingRange = 20;
						boolean isClose = hostile.isInRange(mod.getPlayer(), annoyingRange);

						if (isClose)
						{
							isClose = LookHelper.seesPlayer(hostile, mod.getPlayer(), annoyingRange);
						}
						// Give each hostile a timer, if they're close for too long deal with them.
						if (isClose)
						{

							if (!_closeAnnoyingEntities.containsKey(hostile))
							{
								boolean wardenAttacking = hostile instanceof WardenEntity;
								boolean witherAttacking = hostile instanceof WitherEntity;
								boolean endermanAttacking = hostile instanceof EndermanEntity;
								boolean blazeAttacking = hostile instanceof BlazeEntity;
								boolean witherSkeletonAttacking = hostile instanceof WitherSkeletonEntity;
								boolean hoglinAttacking = hostile instanceof HoglinEntity;
								boolean zoglinAttacking = hostile instanceof ZoglinEntity;
								boolean piglinBruteAttacking = hostile instanceof PiglinBruteEntity;
								boolean vindicatorAttacking = hostile instanceof VindicatorEntity;
								if (blazeAttacking || witherSkeletonAttacking || hoglinAttacking || zoglinAttacking
										|| piglinBruteAttacking || endermanAttacking || witherAttacking
										|| wardenAttacking || vindicatorAttacking)
								{
									if (mod.getPlayer().getHealth() <= 10)
									{
										_closeAnnoyingEntities.put(hostile, new TimerGame(0));
									} else
									{
										_closeAnnoyingEntities.put(hostile, new TimerGame(Float.POSITIVE_INFINITY));
									}
								} else
								{
									_closeAnnoyingEntities.put(hostile, new TimerGame(0));
								}
								_closeAnnoyingEntities.get(hostile).reset();
							}
							if (_closeAnnoyingEntities.get(hostile).elapsed())
							{
								toDealWith.add(hostile);
							}
						} else
						{
							_closeAnnoyingEntities.remove(hostile);
						}
					}
				}
			}

			for (Entity hostile : hostiles)
			{
				int annoyingRange = 25;
				boolean isClose = hostile.isInRange(mod.getPlayer(), annoyingRange);

				if (mod.getClientBaritone().getPathingBehavior().isPathing() && !isClose
						&& mod.getEntityTracker().getHostiles().isEmpty())
				{
					_movementChecker.reset();
				}
			}
			// Clear dead/non existing hostiles
			List<Entity> toRemove = new ArrayList<>();
			if (!_closeAnnoyingEntities.keySet().isEmpty())
			{
				for (Entity check : _closeAnnoyingEntities.keySet())
				{
					if (!check.isAlive())
					{
						toRemove.add(check);
					}
				}
			}
			if (!toRemove.isEmpty())
			{
				for (Entity remove : toRemove)
					_closeAnnoyingEntities.remove(remove);
			}
			int numberOfProblematicEntities = toDealWith.size();
			if (!toDealWith.isEmpty())
			{
				for (Entity ToDealWith : toDealWith)
				{
					if (ToDealWith.getClass() == SlimeEntity.class || ToDealWith.getClass() == MagmaCubeEntity.class
							|| ToDealWith.getWeaponStack() != null && !(ToDealWith instanceof SkeletonEntity ) 
							 && !(ToDealWith instanceof EndermanEntity )
							 && !(ToDealWith instanceof DrownedEntity ))
					{
						// Entities that have a sword or can split into more entities after being killed count as two entities as they are more dangerous then one entity of same type
						numberOfProblematicEntities += 2;
					} else if (ToDealWith instanceof SkeletonEntity && ToDealWith.getWeaponStack().getItem() == Items.BOW)
					{
						// Any skeleton with a bow is REALLY dangerous so we'll count them as 6 entities
						numberOfProblematicEntities += 6;
					} else if (ToDealWith instanceof EndermanEntity)
					{
						// Enderman can be also really dangerous as they hit hard.
						numberOfProblematicEntities += 3;
					} else if (ToDealWith instanceof DrownedEntity && ToDealWith.getWeaponStack().getItem() == Items.TRIDENT)
					{
						// Drowned with tridents are also REALLY dangerous, maybe we should increase this??
						numberOfProblematicEntities += 5;
					}

				}
			}

			if (numberOfProblematicEntities > 0)
			{

				// Depending on our weapons/armor, we may chose to straight up kill hostiles if
				// we're not dodging their arrows.

				// wood 0 : 1 skeleton
				// stone 1 : 1 skeleton
				// iron 2 : 2 hostiles
				// diamond 3 : 3 hostiles
				// netherite 4 : 4 hostiles

				// Armor: (do the math I'm not boutta calculate this)
				// leather: ?1 skeleton
				// iron: ?2 hostiles
				// diamond: ?3 hostiles

				// 7 is full set of leather
				// 15 is full set of iron.
				// 20 is full set of diamond.
				// Diamond+netherite have bonus "toughness" parameter (we can simply add them I
				// think, for now.)
				// full diamond has 8 bonus toughness
				// full netherite has 12 bonus toughness
				int armor = mod.getPlayer().getArmor();
				float damage = bestSword == null ? 0 : (1 + bestSword.getMaterial().getAttackDamage());
				boolean hasShield = mod.getItemStorage().hasItem(Items.SHIELD)
						|| mod.getItemStorage().hasItemInOffhand(Items.SHIELD);
				int shield = hasShield && bestSword != null ? 1 : 0;
				int canDealWith = (int) Math.ceil((armor * 3.6 / 20.0) + (damage * 0.8) + (shield));
				canDealWith += 1;
				if (canDealWith > numberOfProblematicEntities && numberOfProblematicEntities < 3
						&& mod.getPlayer().getHealth() > 10)
				{
					// We can deal with it.
					_runAwayTask = null;
					for (Entity ToDealWith : toDealWith)
					{
						if (ToDealWith instanceof WitchEntity || ToDealWith instanceof PillagerEntity
								|| ToDealWith instanceof PiglinEntity || ToDealWith instanceof StrayEntity
								|| ToDealWith instanceof CaveSpiderEntity)
						{
							mod.getClientBaritoneSettings().avoidance.value = false;
							setTask(new KillEntitiesTask(ToDealWith.getClass()));
							return 65;
						}
						mod.getClientBaritoneSettings().avoidance.value = false;
						setTask(new KillEntitiesTask(ToDealWith.getClass()));
						return 65;
					}
					return 65;
				} else
				{

					for (Entity hostile : toDealWith)
					{
						if (/* hostile.isInRange(mod.getPlayer(), 6) && */toDealWith.size() < 8
								&& !(hostile instanceof SkeletonEntity) && bestSword != null)
						{
							// We can't deal with it
							mod.getClientBaritoneSettings().avoidance.value = false;
							_runAwayTask = new RunAwayFromHostilesTask(
									// Decrease distance from the mob if we are stuck so that baritone gets away faster.
									!_movementChecker.check(mod) ? DANGER_KEEP_DISTANCE : 4, true);
							setTask(_runAwayTask);
							return 80;
						} else if (toDealWith.size() > 8 && !(hostile instanceof SkeletonEntity) && bestSword != null)
						{
							mod.getClientBaritoneSettings().avoidance.value = false;
							_runAwayTask = new RunAwayFromHostilesTask(6, true);
							setTask(_runAwayTask);
							return 80;

						} else
						{
							mod.getClientBaritoneSettings().avoidance.value = false;
							mod.getClientBaritoneSettings().costHeuristic.value = 60.0;
							// If hostile entity is vary close get baritone to keep small distance from the mob and if the mob is far enough away that we can afford to wait for pathfiner to complete its search get baritone to run away further,
							if (mod.getEntityTracker().getClosestEntity((e) -> {
								return e.distanceTo(mod.getPlayer()) < 13;
							}, HostileEntity.class).isPresent())
								_runAwayTask = new RunAwayFromHostilesTask(12, true);
							else
								_runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
							setTask(_runAwayTask);
							return 80;
						}
					}
				}
			} else if ((StorageHelper.getNumberOfThrowawayBlocks(mod) - 1) < 2
					&& mod.getItemStorage().getItemCount(Items.DIRT) < 3
					|| _getThrouwawayBlocks.isActive() && !_getThrouwawayBlocks.isFinished(mod))
			{
				// Get some throw away blocks like dirt so that we can block arrows when we dont have a shield.
				if (mod.getItemStorage().getItemCount(Items.DIRT) >= 16
						|| (StorageHelper.getNumberOfThrowawayBlocks(mod) - 1) > 2)
				{
					_getThrouwawayBlocks.stop(mod);
				} else
				{
					setTask(_getThrouwawayBlocks);
					return 60;
				}
			} else if (bestSword == null)
			{
				// Get a basic weapon if we dont have one already.
				setTask(TaskCatalogue.getItemTask(Items.STONE_SWORD, 1));
				return 60;
			} else if (!mod.getItemStorage().hasItem(Items.SHIELD)
					&& !mod.getItemStorage().hasItemInOffhand(Items.SHIELD)
					|| (mod.getItemStorage().hasItemInOffhand(Items.SHIELD)
							&& StorageHelper.getItemStackInSlot(
									PlayerSlot.OFFHAND_SLOT).getDamage() > Items.SHIELD.getDefaultStack().getMaxDamage()
											* 0.62
							|| mod.getItemStorage().getItemCount(Items.SHIELD) == 1 && StorageHelper
									.getItemStackInSlot(mod.getItemStorage()
											.getSlotsWithItemPlayerInventory(false, Items.SHIELD).get(0))
									.getDamage() > Items.SHIELD.getDefaultStack().getMaxDamage() * 0.62))
			{
				// Get a shield if we dont have a decent one.
				Boolean hasGoodShield = false;
				for (Slot slot : mod.getItemStorage().getSlotsWithItemScreen(Items.SHIELD))
				{
					if (StorageHelper.getItemStackInSlot(slot).getDamage() < Items.SHIELD.getDefaultStack().getMaxDamage() * 0.62)
					{
						hasGoodShield = true;
						break;
					}
				}
				if (!hasGoodShield)
				{
					setTask(TaskCatalogue.getItemTask(Items.SHIELD, 2));
					return 55;
				}
			} else
			{
				for (Item item : SWORDS)
				{
					if (mod.getItemStorage().getItemCount(SWORDS) == 1
							&& mod.getItemStorage().getSlotsWithItemPlayerInventory(false, item).size() != 0
							&& StorageHelper
									.getItemStackInSlot(
											mod.getItemStorage().getSlotsWithItemPlayerInventory(false, item).get(0))
									.getDamage() > item.getDefaultStack().getMaxDamage() * 0.7)
					{
						// Get a good sword if the one we have is 70% damaged
						if (mod.getItemStorage().hasItem(Items.STONE_SWORD))
							setTask(TaskCatalogue.getItemTask(Items.IRON_SWORD, 1));
						else if (mod.getItemStorage().getItemCount(Items.STONE_SWORD) == 0)
							setTask(TaskCatalogue.getItemTask(Items.STONE_SWORD, 1));

						return 60;
					}
				}
			}
		}
		// By default if we aren't "immediately" in danger but were running away, keep
		// running away until we're good.
		if (_runAwayTask != null && !_runAwayTask.isFinished(mod))
		{
			setTask(_runAwayTask);
			return _cachedLastPriority;
		} else
		{
			_runAwayTask = null;
		}
		return 0;
	}

	private BlockPos isInsideFireAndOnFire(AltoClef mod)
	{
		boolean onFire = mod.getPlayer().isOnFire();
		if (!onFire) return null;
		BlockPos p = mod.getPlayer().getBlockPos();
		BlockPos[] toCheck = new BlockPos[] {
				p, p.add(1, 0, 0), p.add(1, 0, -1), p.add(0, 0, -1), p.add(-1, 0, -1), p.add(-1, 0, 0), p.add(-1, 0, 1),
				p.add(0, 0, 1), p.add(1, 0, 1)
		};
		for (BlockPos check : toCheck)
		{
			Block b = mod.getWorld().getBlockState(check).getBlock();
			if (b instanceof AbstractFireBlock)
			{
				return check;
			}
		}
		return null;
	}

	private void putOutFire(AltoClef mod, BlockPos pos)
	{
		Optional<Rotation> reach = LookHelper.getReach(pos);
		if (reach.isPresent())
		{
			Baritone b = mod.getClientBaritone();
			if (LookHelper.isLookingAt(mod, pos))
			{
				b.getPathingBehavior().requestPause();
				b.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
				return;
			}
			LookHelper.lookAt(mod, reach.get());
		}
	}

	private void doForceField(AltoClef mod)
	{

		_killAura.tickStart();

		// Hit all hostiles close to us.
		List<Entity> entities = mod.getEntityTracker().getCloseEntities();
		try
		{
			if (!entities.isEmpty())
			{
				for (Entity entity : entities)
				{
					boolean shouldForce = false;
					if (mod.getBehaviour().shouldExcludeFromForcefield(entity)) continue;
					if (entity instanceof MobEntity)
					{
						if (EntityHelper.isGenerallyHostileToPlayer(mod, entity))
						{
							if (LookHelper.seesPlayer(entity, mod.getPlayer(), 10))
							{
								shouldForce = true;
							}
						}
					} else if (entity instanceof FireballEntity)
					{
						// Ghast ball
						shouldForce = true;
					} else if (entity instanceof PlayerEntity player && mod.getBehaviour().shouldForceFieldPlayers())
					{
						if (!player.equals(mod.getPlayer()))
						{
							String name = player.getName().getString();
							if (!mod.getButler().isUserAuthorized(name))
							{
								shouldForce = true;
							}
						}
					}
					if (shouldForce)
					{
						applyForceField(entity);
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		_killAura.tickEnd(mod);
	}

	private void applyForceField(Entity entity)
	{
		_killAura.applyAura(entity);
	}

	private CreeperEntity getClosestFusingCreeper(AltoClef mod)
	{
		double worstSafety = Float.POSITIVE_INFINITY;
		CreeperEntity target = null;
		try
		{
			List<CreeperEntity> creepers = mod.getEntityTracker().getTrackedEntities(CreeperEntity.class);
			if (!creepers.isEmpty())
			{
				for (CreeperEntity creeper : creepers)
				{
					if (creeper == null) continue;
					if (creeper.getClientFuseTime(1) < 0.001) continue;

					// We want to pick the closest creeper, but FIRST pick creepers about to blow
					// At max fuse, the cost goes to basically zero.
					double safety = getCreeperSafety(mod.getPlayer().getPos(), creeper);
					if (safety < worstSafety)
					{
						target = creeper;
					}
				}
			}
		} catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException | NullPointerException e)
		{
			// IDK why but these exceptions happen sometimes. It's extremely bizarre and I
			// have no idea why.
			Debug.logWarning("Weird Exception caught and ignored while scanning for creepers: " + e.getMessage());
			return target;
		}
		return target;
	}

	private boolean isProjectileClose(AltoClef mod)
	{
		List<CachedProjectile> projectiles = mod.getEntityTracker().getProjectiles();
		// Find a skeleton that is about to shoot.
		Optional<Entity> entity = mod.getEntityTracker().getClosestEntity((e) -> {
			if (e instanceof SkeletonEntity
					&& (EntityHelper.isAngryAtPlayer(mod, e) || ((SkeletonEntity) e).getItemUseTime() > 18)
					&& ((((SkeletonEntity) e).distanceTo(mod.getPlayer()) < 7
							&& ((SkeletonEntity) e).getItemUseTime() > 10)
							|| ((SkeletonEntity) e).getItemUseTime() > 13))
				return true;
			return false;
		}, SkeletonEntity.class);
		try
		{
			if (!projectiles.isEmpty())
			{
				for (CachedProjectile projectile : projectiles)
				{
					if (projectile.position.squaredDistanceTo(mod.getPlayer().getPos()) < 150)
					{
						boolean isGhastBall = projectile.projectileType == FireballEntity.class;
						if (isGhastBall)
						{
							Optional<Entity> ghastBall = mod.getEntityTracker().getClosestEntity(FireballEntity.class);
							Optional<Entity> ghast = mod.getEntityTracker().getClosestEntity(GhastEntity.class);
							if (ghastBall.isPresent() && ghast.isPresent() && _runAwayTask == null
									&& mod.getClientBaritone().getPathingBehavior().isSafeToCancel())
							{
								mod.getClientBaritone().getPathingBehavior().requestPause();
								LookHelper.lookAt(mod, ghast.get().getEyePos());
							}
							return false;
							// Ignore ghast balls
						}
						if (projectile.projectileType == DragonFireballEntity.class)
						{
							// Ignore dragon fireballs
							return false;
						}

						Vec3d expectedHit = ProjectileHelper.calculateArrowClosestApproach(projectile, mod.getPlayer());

						Vec3d delta = mod.getPlayer().getPos().subtract(expectedHit);

						double horizontalDistanceSq = delta.x * delta.x + delta.z * delta.z;
						double verticalDistance = Math.abs(delta.y);
						if (horizontalDistanceSq < ARROW_KEEP_DISTANCE_HORIZONTAL * ARROW_KEEP_DISTANCE_HORIZONTAL
								&& verticalDistance < ARROW_KEEP_DISTANCE_VERTICAL)
						{
							if (mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
									&& (mod.getItemStorage().hasItem(Items.SHIELD)
											|| mod.getItemStorage().hasItemInOffhand(Items.SHIELD)))
							{
								mod.getClientBaritone().getPathingBehavior().requestPause();
								LookHelper.lookAt(mod, projectile.position.add(0, 0.3, 0));
							}
							return true;
						}
					}
				}
			}
			// Check if any close by entities are going to attack us and look at them to block the attack.
			if (entity.isPresent() && mod.getEntityTracker().getClosestEntity((e) -> {
				if ((e instanceof SpiderEntity || e instanceof ZombieEntity || e instanceof ZombieVillagerEntity
						|| e instanceof CaveSpiderEntity) && EntityHelper.isAngryAtPlayer(mod, e)
						&& e.distanceTo(mod.getPlayer()) < 4)
					return true;
				return false;
			}, SkeletonEntity.class).isEmpty())
			{

				if (mod.getClientBaritone().getPathingBehavior().isSafeToCancel()
						&& (mod.getItemStorage().hasItem(Items.SHIELD)
								|| mod.getItemStorage().hasItemInOffhand(Items.SHIELD)))
				{
					mod.getClientBaritone().getPathingBehavior().requestPause();
					LookHelper.lookAt(mod, entity.get().getEyePos());
				}
				return true;
			}
		} catch (ConcurrentModificationException ignored)
		{
		}
		return false;
	}

	private Optional<Entity> getUniversallyDangerousMob(AltoClef mod)
	{
		// Wither skeletons are dangerous because of the wither effect. Oof kinda
		// obvious.
		// If we merely force field them, we will run into them and get the wither
		// effect which will kill us.
		Optional<Entity> warden = mod.getEntityTracker().getClosestEntity(WardenEntity.class);
		if (warden.isPresent())
		{
			double range = SAFE_KEEP_DISTANCE - 2;
			if (warden.get().squaredDistanceTo(mod.getPlayer()) < range * range
					&& EntityHelper.isAngryAtPlayer(mod, warden.get()))
			{
				return warden;
			}
		}
		Optional<Entity> wither = mod.getEntityTracker().getClosestEntity(WitherEntity.class);
		if (wither.isPresent())
		{
			double range = SAFE_KEEP_DISTANCE - 2;
			if (wither.get().squaredDistanceTo(mod.getPlayer()) < range * range
					&& EntityHelper.isAngryAtPlayer(mod, wither.get()))
			{
				return wither;
			}
		}
		Optional<Entity> witherSkeleton = mod.getEntityTracker().getClosestEntity(WitherSkeletonEntity.class);
		if (witherSkeleton.isPresent())
		{
			double range = SAFE_KEEP_DISTANCE - 2;
			if (witherSkeleton.get().squaredDistanceTo(mod.getPlayer()) < range * range
					&& EntityHelper.isAngryAtPlayer(mod, witherSkeleton.get()))
			{
				return witherSkeleton;
			}
		}
		// Hoglins are dangerous because we can't push them with the force field.
		// If we merely force field them and stand still our health will slowly be
		// chipped away until we die
		Optional<Entity> hoglin = mod.getEntityTracker().getClosestEntity(HoglinEntity.class);
		if (hoglin.isPresent())
		{
			double range = SAFE_KEEP_DISTANCE - 2;
			if (hoglin.get().squaredDistanceTo(mod.getPlayer()) < range * range
					&& EntityHelper.isAngryAtPlayer(mod, hoglin.get()))
			{
				return hoglin;
			}
		}
		Optional<Entity> zoglin = mod.getEntityTracker().getClosestEntity(ZoglinEntity.class);
		if (zoglin.isPresent())
		{
			double range = SAFE_KEEP_DISTANCE - 2;
			if (zoglin.get().squaredDistanceTo(mod.getPlayer()) < range * range
					&& EntityHelper.isAngryAtPlayer(mod, zoglin.get()))
			{
				return zoglin;
			}
		}
		Optional<Entity> piglinBrute = mod.getEntityTracker().getClosestEntity(PiglinBruteEntity.class);
		if (piglinBrute.isPresent())
		{
			double range = SAFE_KEEP_DISTANCE - 2;
			if (piglinBrute.get().squaredDistanceTo(mod.getPlayer()) < range * range
					&& EntityHelper.isAngryAtPlayer(mod, piglinBrute.get()))
			{
				return piglinBrute;
			}
		}
		Optional<Entity> vindicator = mod.getEntityTracker().getClosestEntity(VindicatorEntity.class);
		if (vindicator.isPresent())
		{
			double range = SAFE_KEEP_DISTANCE - 2;
			if (vindicator.get().squaredDistanceTo(mod.getPlayer()) < range * range
					&& EntityHelper.isAngryAtPlayer(mod, vindicator.get()))
			{
				return vindicator;
			}
		}
		return Optional.empty();
	}

	private boolean isInDanger(AltoClef mod)
	{
		Optional<Entity> witch = mod.getEntityTracker().getClosestEntity(WitchEntity.class);
		boolean hasFood = mod.getFoodChain().hasFood();
		float health = mod.getPlayer().getHealth();
		if (health <= 10 && hasFood && witch.isEmpty())
		{
			return true;
		}
		if (mod.getPlayer().hasStatusEffect(StatusEffects.WITHER)
				|| (mod.getPlayer().hasStatusEffect(StatusEffects.POISON) && witch.isEmpty()))
		{
			return true;
		}
		if (WorldHelper.isVulnurable(mod))
		{
			// If hostile mobs are nearby...
			try
			{
				ClientPlayerEntity player = mod.getPlayer();
				List<Entity> hostiles = mod.getEntityTracker().getHostiles();
				if (!hostiles.isEmpty())
				{
					synchronized (BaritoneHelper.MINECRAFT_LOCK)
					{
						for (Entity entity : hostiles)
						{
							if (entity.isInRange(player, SAFE_KEEP_DISTANCE)
									&& !mod.getBehaviour().shouldExcludeFromForcefield(entity)
									&& EntityHelper.isAngryAtPlayer(mod, entity))
							{
								return true;
							}
						}
					}
				}
			} catch (Exception e)
			{
				Debug.logWarning("Weird multithread exception. Will fix later.");
			}
		}
		return false;
	}

	public void setTargetEntity(Entity entity)
	{
		_targetEntity = entity;
	}

	public void resetTargetEntity()
	{
		_targetEntity = null;
	}

	public void setForceFieldRange(double range)
	{
		_killAura.setRange(range);
	}

	public void resetForceField()
	{
		_killAura.setRange(Double.POSITIVE_INFINITY);
	}

	public boolean isDoingAcrobatics()
	{
		return _doingFunkyStuff;
	}

	public boolean isPuttingOutFire()
	{
		return _wasPuttingOutFire;
	}

	@Override
	public boolean isActive()
	{
		// We're always checking for mobs
		return true;
	}

	@Override
	protected void onTaskFinish(AltoClef mod)
	{
		// Task is done, so I guess we move on?
	}

	@Override
	public String getName()
	{
		return "Mob Defense";
	}
}