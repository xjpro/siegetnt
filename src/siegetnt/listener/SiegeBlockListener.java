package siegetnt.listener;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

public class SiegeBlockListener implements Listener {

    private final Material PLACEMENT_MATERIAL = Material.REDSTONE_BLOCK;
    private final Material CURING_MATERIAL = Material.AIR;
    private final Material CURED_MATERIAL = Material.GLOWSTONE;
    private final int CONVERT_TIME = 100; // 20 = 1 second

    private final Plugin plugin;

    public SiegeBlockListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        if (block.getType() == PLACEMENT_MATERIAL) {

            block.setType(CURING_MATERIAL);

            int curingEffectTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                block.getWorld().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.MOBSPAWNER_FLAMES, 0);
            }, 0, 5);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Bukkit.getScheduler().cancelTask(curingEffectTaskId);
                block.getWorld().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.ZOMBIE_DESTROY_DOOR, 0);

                // If the block is up against the ground or another cured block, place it
                if (block.getRelative(BlockFace.DOWN).getType().isSolid() ||
                        block.getRelative(BlockFace.NORTH).getType() == CURED_MATERIAL ||
                        block.getRelative(BlockFace.EAST).getType() == CURED_MATERIAL ||
                        block.getRelative(BlockFace.SOUTH).getType() == CURED_MATERIAL ||
                        block.getRelative(BlockFace.WEST).getType() == CURED_MATERIAL) {
                    block.setType(CURED_MATERIAL);
                }
                // Otherwise, spawn it as a falling block
                else {
                    block.setType(Material.AIR); // In case something was placed here
                    block.getWorld().spawnFallingBlock(block.getLocation(), CURED_MATERIAL, block.getData());
                }

            }, CONVERT_TIME);
        }
    }
}
