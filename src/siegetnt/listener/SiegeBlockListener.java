package siegetnt.listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class SiegeBlockListener implements Listener {

	private final Material PLACEMENT_MATERIAL = Material.MAGMA;
	private final Material CURING_MATERIAL = Material.AIR;
	private final Material CURED_MATERIAL = Material.NETHERRACK;
	private final int CONVERT_TIME = 80; // 20 = 1 second
	private ArrayList<Location> convertingLocations = new ArrayList<>();

	private final Plugin plugin;

	public SiegeBlockListener(Plugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;

		Block block = event.getBlock();
		if (block.getType() == PLACEMENT_MATERIAL) {
			boolean isCancelled = false;
			if (!isSupported(block)) {
				isCancelled = true;
				event.getPlayer().sendMessage(ChatColor.RED + "Block must be supported by the ground or attached to a nether block");
			}

			if (convertingLocations.stream().anyMatch(location -> location.equals(block.getLocation()))) {
				isCancelled = true; // Already a placement here
			}

			if (isCancelled) {
				event.setCancelled(true);
				event.setBuild(false);
				return;
			}

			block.setType(CURING_MATERIAL);
			convertingLocations.add(block.getLocation());

			int curingEffectTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
				block.getWorld().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.MOBSPAWNER_FLAMES, 0);
			}, 0, 5);

			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				Bukkit.getScheduler().cancelTask(curingEffectTaskId);
				block.getWorld().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.ZOMBIE_CHEW_WOODEN_DOOR, 0);

				// If the block is up against the ground or another cured block, place it
				if (isSupported(block)) {
					block.setType(CURED_MATERIAL);
				}
				// Otherwise, spawn it as a falling block
				else {
					block.setType(Material.AIR); // In case something was placed here
					block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0, 0.5), CURED_MATERIAL, block.getData());
				}

				convertingLocations.remove(block.getLocation());

			}, CONVERT_TIME);
		}
	}

	private boolean isSupported(Block block) {
		return block.getRelative(BlockFace.DOWN).getType().isSolid() ||
				block.getRelative(BlockFace.NORTH).getType() == CURED_MATERIAL ||
				block.getRelative(BlockFace.EAST).getType() == CURED_MATERIAL ||
				block.getRelative(BlockFace.SOUTH).getType() == CURED_MATERIAL ||
				block.getRelative(BlockFace.WEST).getType() == CURED_MATERIAL;
	}
}
