package siegetnt.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import siegetnt.ExplosionBlock;
import siegetnt.ShockRadiusTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExplosionListener implements Listener {

	private final Random random = new Random();
	private final ShockRadiusTracker shockRadiusTracker;

	public ExplosionListener(ShockRadiusTracker shockRadiusTracker) {
		this.shockRadiusTracker = shockRadiusTracker;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplode(EntityExplodeEvent event) {
		Location loc = event.getLocation();
		World world = loc.getWorld();

		Entity explodedEntity = event.getEntity();
		boolean explosionDamagesBlocks = true;

		if (explodedEntity == null) {
			// Triggered explosions have no entity, so make a fake one
			explodedEntity = world.spawnEntity(loc, EntityType.PRIMED_TNT);
		} else if (explodedEntity.getType() == EntityType.CREEPER) {
			// Creeper explosions do not do anything
			// TODO should SiegeTNT really care about this?
			explosionDamagesBlocks = false;
		}

		// Event would be cancelled if it was damaging something that it shouldn't be, we will honor that
		// but still want the explosion animation and damage to player to occur
		if (event.isCancelled()) {
			explosionDamagesBlocks = false;
			event.setCancelled(false);
		}

		// Damage from the explosion needs to be modded back in
		double blastDistance = 4;
		for (Entity nearby : explodedEntity.getNearbyEntities(blastDistance, blastDistance, blastDistance)) {
			if (nearby instanceof LivingEntity && !nearby.isDead()) {
				LivingEntity p = (LivingEntity) nearby;
				double distanceFromExplosion = p.getLocation().distance(loc);
				int damageTaken = (int) Math.round(25 * (1 - (distanceFromExplosion / blastDistance)));
				p.damage(Math.max(0, damageTaken));
			}
		}

		explodedEntity.remove();

		if (!explosionDamagesBlocks) {
			// This would be like a creeper explosion; no block damage will occur
			return;
		}

		shockRadiusTracker.addShockRadiusLocation(loc);

		// Block destruction code
		List<ExplosionBlock> explosionBlocks = new ArrayList<>();

		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ()), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), false)); // center
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY(), loc.getBlockZ()), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ() - 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() - 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY(), loc.getBlockZ() - 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ() + 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() + 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY(), loc.getBlockZ() + 1), false));

		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY() + 1, loc.getBlockZ()), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()), false)); // one up
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY() + 1, loc.getBlockZ()), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY() + 1, loc.getBlockZ() - 1), true));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ() - 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY() + 1, loc.getBlockZ() - 1), true));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY() + 1, loc.getBlockZ() + 1), true));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ() + 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY() + 1, loc.getBlockZ() + 1), true));

		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY() - 1, loc.getBlockZ()), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()), false)); // one down
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY() - 1, loc.getBlockZ()), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY() - 1, loc.getBlockZ() - 1), true));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ() - 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY() - 1, loc.getBlockZ() - 1), true));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() - 1, loc.getBlockY() - 1, loc.getBlockZ() + 1), true));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ() + 1), false));
		explosionBlocks.add(new ExplosionBlock(loc.getWorld().getBlockAt(loc.getBlockX() + 1, loc.getBlockY() - 1, loc.getBlockZ() + 1), true));

		for (ExplosionBlock explosionBlock : explosionBlocks) {
			Block block = explosionBlock.getBlock();
			boolean isCorner = explosionBlock.isCorner();

			switch (block.getType()) {
				case TNT:
					Bukkit.getPluginManager().callEvent(new EntityChangeBlockEvent(explodedEntity, block, Material.AIR, block.getData()));
					block.setType(Material.AIR);
					Bukkit.getPluginManager().callEvent(new EntityExplodeEvent(null, block.getLocation(), new ArrayList<>(), 0));
					//block.getWorld().createExplosion(block.getLocation(), 0);
					break;
				case AIR: // can't blow up air!
				case BEDROCK:
				case IRON_DOOR_BLOCK:
					// Immune blocks
					break;
				case DIAMOND_BLOCK:
				case IRON_BLOCK:
				case GOLD_BLOCK:
					if (!isCorner || random.nextDouble() < 0.05) {
						Bukkit.getPluginManager().callEvent(new EntityChangeBlockEvent(explodedEntity, block, Material.OBSIDIAN, block.getData()));
						block.setType(Material.OBSIDIAN);
					}
					break;
				case OBSIDIAN:
					if (!isCorner || random.nextDouble() < 0.10) {
						Bukkit.getPluginManager().callEvent(new EntityChangeBlockEvent(explodedEntity, block, Material.STONE, block.getData()));
						block.setType(Material.STONE);
					}
					break;
				case STONE:
				case SMOOTH_BRICK:
				case SMOOTH_STAIRS:
				case DOUBLE_STEP:
				case BRICK:
				case BRICK_STAIRS:
				case NETHER_BRICK:
				case NETHER_BRICK_STAIRS:
				case PISTON_BASE:
				case PISTON_STICKY_BASE:
				case IRON_ORE:
				case REDSTONE_ORE:
				case GOLD_ORE:
				case DIAMOND_ORE:
				case EMERALD_ORE:
					if (!isCorner || random.nextDouble() < 0.30) {
						Bukkit.getPluginManager().callEvent(new EntityChangeBlockEvent(explodedEntity, block, Material.COBBLESTONE, block.getData()));
						block.setType(Material.COBBLESTONE);
					}
					break;
				case COBBLESTONE:
				case COBBLESTONE_STAIRS:
				case MOSSY_COBBLESTONE:
				case FURNACE:
				case DISPENSER:
					if (!isCorner || random.nextDouble() < 0.60) {
						Bukkit.getPluginManager().callEvent(new EntityChangeBlockEvent(explodedEntity, block, Material.GRAVEL, block.getData()));
						block.setType(Material.GRAVEL);
					}
					break;
				case SANDSTONE:
				case SANDSTONE_STAIRS:
				case SOUL_SAND:
					if (!isCorner || random.nextDouble() < 0.60) {
						Bukkit.getPluginManager().callEvent(new EntityChangeBlockEvent(explodedEntity, block, Material.SAND, block.getData()));
						block.setType(Material.SAND);
					}
					break;
				case ENDER_STONE: // very vulnerable!
				default:
					// Everything else drops
					if (!isCorner || random.nextDouble() < 0.90) {
						Bukkit.getPluginManager().callEvent(new EntityChangeBlockEvent(explodedEntity, block, Material.AIR, block.getData()));
						block.setType(Material.AIR);
					}
					break;
				// ItemStack item = new ItemStack(block.getType(), 1);
				// world.dropItem(block.getLocation(), item);
			}
		}
		// End block destruction
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		event.setRadius(0f); // No damage to blocks, but the animation will still occur
	}
}
