package siegetnt.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import siegetnt.ShockRadiusTracker;

import java.util.Arrays;
import java.util.List;

public class WorldListener implements Listener {

    private static List<Material> ALLOWED_BLOCKS = Arrays.asList(Material.TNT, Material.FIRE, Material.MAGMA);
    private final ShockRadiusTracker shockRadiusTracker;

    public WorldListener(ShockRadiusTracker shockRadiusTracker) {
        this.shockRadiusTracker = shockRadiusTracker;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void preventBlockPlacementInShockRadius(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        if (ALLOWED_BLOCKS.stream().noneMatch(type -> type == block.getType()) && shockRadiusTracker.isInShockLocation(block.getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "A recent explosion has made the area too unstable to build");
        }
    }
}
