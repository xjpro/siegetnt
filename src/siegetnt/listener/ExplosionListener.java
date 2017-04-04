package siegetnt.listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import siegetnt.ShockRadiusTracker;
import siegetnt.SiegeTNTPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

public class ExplosionListener implements Listener {

	private final Random random = new Random();
	private final SiegeTNTPlugin plugin;
	private final ShockRadiusTracker shockRadiusTracker;

	public ExplosionListener(SiegeTNTPlugin plugin, ShockRadiusTracker shockRadiusTracker) {
		this.plugin = plugin;
		this.shockRadiusTracker = shockRadiusTracker;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplode(EntityExplodeEvent event) {
		Location location = event.getLocation();

		Entity explodedEntity = event.getEntity();
		if (explodedEntity == null) {
			// Need an entity for block change event but don't actually want it in world
			explodedEntity = location.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);
			explodedEntity.remove();
		}

		causeExplosiveDamage(location, explodedEntity.getType());

		if (event.isCancelled() || (explodedEntity.getType() == EntityType.CREEPER || explodedEntity.getType() == EntityType.GHAST)) {
			// Two cases where we don't want to cause any block damage:
			// 1. Event is cancelled, probably because it was damaging something protected
			// 2. Explosion caused by a monster TODO should SiegeTNT really care about this?
			return;
		}

		// Block degrading code
		Collection<Block> explodedBlocks = getExplodedBlocks(location);
		for (Block block : explodedBlocks) {
			boolean isCorner = block.getMetadata("corner").get(0).asBoolean();
			Material to = null;
			switch (block.getType()) {
				case TNT:
					// Note: first TNT to go off will not ever hit here as it's turned into an explosion or entity by Minecraft
					to = Material.AIR;

					block.getLocation().getWorld().playEffect(block.getLocation(), Effect.EXPLOSION_LARGE, 0);
					causeExplosiveDamage(block.getLocation(), EntityType.PRIMED_TNT);

					// Add a shock radius, preventing building in this area
					shockRadiusTracker.addShockRadiusLocation(block.getLocation());
					break;
				// Immune
				case BEDROCK:
					if (block.getRelative(BlockFace.UP).getType() == Material.IRON_DOOR_BLOCK) {
						// Break doors attached to bedrock
						block.getRelative(BlockFace.UP).breakNaturally();
					}
					break;
				case AIR: // can't blow up air!
				case IRON_DOOR_BLOCK:
					break;
				// 5 protection
				case DIAMOND_BLOCK:
				case EMERALD_BLOCK:
				case IRON_BLOCK:
				case GOLD_BLOCK:
					if (!isCorner || random.nextDouble() < 0.05) {
						to = Material.OBSIDIAN;
					}
					break;
				// 4 protection
				case OBSIDIAN:
					if (!isCorner || random.nextDouble() < 0.10) {
						to = Material.STONE;
					}
					break;
				// 3 protection
				case STONE:
				case SMOOTH_BRICK:
				case SMOOTH_STAIRS:
				case DOUBLE_STEP:
				case BRICK:
				case BRICK_STAIRS:
				case PRISMARINE:
				case NETHER_BRICK:
				case NETHER_BRICK_STAIRS:
				case PISTON_BASE:
				case PISTON_STICKY_BASE:
				case IRON_ORE:
				case REDSTONE_ORE:
				case GOLD_ORE:
				case DIAMOND_ORE:
				case EMERALD_ORE:
				case HARD_CLAY:
				case STAINED_CLAY:
					if (!isCorner || random.nextDouble() < 0.30) {
						to = Material.COBBLESTONE;
					}
					break;
				// 2 protection
				case COBBLESTONE:
				case COBBLESTONE_STAIRS:
				case MOSSY_COBBLESTONE:
				case FURNACE:
				case DISPENSER:
				case HOPPER:
					if (!isCorner || random.nextDouble() < 0.60) {
						to = Material.GRAVEL;
					}
					break;
				case SANDSTONE:
				case SANDSTONE_STAIRS:
				case SOUL_SAND:
					if (!isCorner || random.nextDouble() < 0.60) {
						to = Material.SAND;
					}
					break;
				// 1 protection
				case ENDER_STONE: // very vulnerable!
				case END_BRICKS:
				default:
					// Everything else drops
					if (!isCorner || random.nextDouble() < 0.90) {
						to = Material.AIR;
					}
					break;
				// ItemStack item = new ItemStack(block.getType(), 1);
				// world.dropItem(block.getLocation(), item);
			}

			if (to != null) {
				plugin.logRemoval(block);
				if (to != Material.AIR) {
					plugin.logPlacement(block, to);
				}
				Bukkit.getPluginManager().callEvent(new EntityChangeBlockEvent(explodedEntity, block, to, block.getData()));
				block.setType(to);
			}

			// Clean up metadata
			block.removeMetadata("corner", plugin);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		event.setRadius(0f); // No damage to blocks, but the animation will still occur
	}

	private Collection<Block> getExplodedBlocks(Location location) {
		// Put into a map so we don't have any duplicates
		HashMap<Location, Block> explodedBlocks = new HashMap<>();

		Stack<Block> blocksToAdd = getSurroundingBlocks(location.getWorld().getBlockAt(location));
		while (blocksToAdd.size() > 0) {
			Block block = blocksToAdd.pop();

			if (explodedBlocks.containsKey(block.getLocation())) {
				block.setMetadata("corner", new FixedMetadataValue(plugin, false));
			} else {
				if (block.getType() == Material.TNT) {
					// One of the block is another TNT block, add it to queue
					getSurroundingBlocks(block).forEach(blocksToAdd::push);
				}
				explodedBlocks.put(block.getLocation(), block);
			}
		}

		return explodedBlocks.values();
	}

	private Stack<Block> getSurroundingBlocks(Block center) {
		Stack<Block> explodedBlocks = new Stack<>();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					boolean isCorner = x != 0 && y != 0 && z != 0;
					Block block = center.getRelative(x, y, z);
					block.setMetadata("corner", new FixedMetadataValue(plugin, isCorner));
					explodedBlocks.add(block);
				}
			}
		}
		return explodedBlocks;
	}

	private void causeExplosiveDamage(Location location, EntityType exploded) {
		// Damage from the explosion needs to be modded back in
		double blastDistance = 4;
		for (Entity nearby : location.getWorld().getNearbyEntities(location, 4, 4, 4)) {
			if (nearby instanceof LivingEntity && !nearby.isDead()) {
				LivingEntity entity = (LivingEntity) nearby;
				double distanceFromExplosion = entity.getLocation().distance(location);
				int damageTaken = Math.max(0, (int) Math.round(25 * (1 - (distanceFromExplosion / blastDistance))));
				EntityDamageEvent damageEvent = new EntityDamageEvent(entity,
						exploded == EntityType.CREEPER || exploded == EntityType.GHAST ? EntityDamageEvent.DamageCause.ENTITY_EXPLOSION : EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
						damageTaken);
				entity.setLastDamageCause(damageEvent);
				entity.setLastDamage(damageTaken);
				entity.damage(damageTaken);
			}
		}
	}
}
