package siegetnt.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import siegetnt.ShockRadiusTracker;

public class WorldListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();

        if (block.getType() == Material.TNT || block.getType() == Material.FIRE) {
            return; // TNT is always allowed
        } else if (ShockRadiusTracker.isInShockLocation(block.getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "A recent explosion has made the area unstable");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.TNT) {
            // no fuse time on TNT - get rid of the block and make an explosion
            event.setCancelled(true);
            block.setType(Material.AIR);
            block.getWorld().createExplosion(block.getLocation(), 0);
        }
    }
}
