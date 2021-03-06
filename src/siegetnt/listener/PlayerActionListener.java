package siegetnt.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class PlayerActionListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) return;

		// Replace TNT block with a primed TNT entity if player walks on it
		Block belowBlock = event.getPlayer().getWorld().getBlockAt(event.getFrom().add(0, -1, 0));
		if (belowBlock.getType() == Material.TNT) {
			belowBlock.setType(Material.AIR);
			event.getPlayer().getWorld().spawn(belowBlock.getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) return;

		// Notch decided to make right click with flint not set TNT on fire anymore.
		// This block of code replaces that functionality.
		if (clickedBlock.getType() == Material.TNT
				&& event.getItem() != null
				&& event.getItem().getType() == Material.FLINT_AND_STEEL
				&& event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			event.setCancelled(true);
			// Instead, set block on fire
			BlockFace[] facesToCheck = {BlockFace.UP, BlockFace.DOWN,
					BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
			for (BlockFace face : facesToCheck) {
				Block neighbor = clickedBlock.getRelative(face);
				if (neighbor.getType().equals(Material.AIR)) {
					neighbor.setType(Material.FIRE);
					return;
				}
			}
		}
		// End TNT fire hack
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBurn(BlockBurnEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.TNT) {
			// no fuse time on TNT - get rid of the block and make an explosion
			event.setCancelled(true);
			block.setType(Material.AIR);
			block.getWorld().createExplosion(block.getLocation(), 0);
			Bukkit.getPluginManager().callEvent(new EntityExplodeEvent(null, block.getLocation(), new ArrayList<>(), 0));
		}
	}
}
