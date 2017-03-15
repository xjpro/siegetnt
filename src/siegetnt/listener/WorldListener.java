package siegetnt.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import siegetnt.ShockRadius;
import siegetnt.ShockRadiusTracker;

import java.util.Arrays;
import java.util.Date;
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
		if (ALLOWED_BLOCKS.stream().anyMatch(type -> type == event.getBlock().getType())) return;

		ShockRadius latestShockRadius = shockRadiusTracker.getLatestShockRadius(event.getBlock().getLocation());
		if (latestShockRadius != null) {
			event.setCancelled(true);

			long secondsRemaining = ShockRadiusTracker.SHOCK_RADIUS_SECONDS - (((new Date()).getTime() - latestShockRadius.getCreated().getTime()) / 1000);
			String timeRemaining;
			if (secondsRemaining > 60) {
				timeRemaining = (secondsRemaining / 60) + "m " + (secondsRemaining % 60) + "s";
			} else {
				timeRemaining = (secondsRemaining % 60) + "s";
			}
			event.getPlayer().sendMessage(String.format("%sA recent explosion has made the area too unstable to build\n(%s remaining)", ChatColor.RED, timeRemaining));
		}
	}
}
