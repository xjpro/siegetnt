package siegetnt;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import siegetnt.listener.ExplosionListener;
import siegetnt.listener.PlayerActionListener;
import siegetnt.listener.SiegeBlockListener;
import siegetnt.listener.WorldListener;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

public class SiegeTNTPlugin extends JavaPlugin {

	private final ShockRadiusTracker shockRadiusTracker = new ShockRadiusTracker();

	@Override
	public void onEnable() {
		PluginManager manager = this.getServer().getPluginManager();
		manager.registerEvents(new ExplosionListener(this, shockRadiusTracker), this);
		manager.registerEvents(new WorldListener(shockRadiusTracker), this);
		manager.registerEvents(new PlayerActionListener(), this);
		manager.registerEvents(new SiegeBlockListener(this, shockRadiusTracker), this);

		replaceSiegeBlockRecipe();
		Logger.getLogger("Minecraft").info("SiegeTNT enabled");
	}

	public void logRemoval(Block block) {
		if (getCoreProtect() != null) {
			getCoreProtect().logRemoval("SiegeTNTPlugin", block.getLocation(), block.getType(), block.getData());
		}
	}

	public void logPlacement(Block block, Material to) {
		if (getCoreProtect() != null) {
			getCoreProtect().logPlacement("SiegeTNTPlugin", block.getLocation(), to, block.getData());
		}
	}

	private CoreProtectAPI getCoreProtect() {
		Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

		// Check that CoreProtect is loaded
		if (plugin == null || !(plugin instanceof CoreProtect)) {
			return null;
		}

		// Check that the API is enabled
		CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
		if (!CoreProtect.isEnabled()) {
			return null;
		}

		// Check that a compatible version of the API is loaded
		if (CoreProtect.APIVersion() < 4) {
			return null;
		}

		return CoreProtect;
	}

	private void replaceSiegeBlockRecipe() {
		Iterator<Recipe> it = getServer().recipeIterator();
		Recipe recipe;
		while (it.hasNext()) {
			recipe = it.next();
			if (recipe != null && recipe.getResult().getType() == Material.MAGMA) {
				it.remove();
			}
		}

		ItemStack siegeBlockItem = new ItemStack(Material.MAGMA, 1);
		ItemMeta itemMeta = siegeBlockItem.getItemMeta();
		itemMeta.setDisplayName("Siege Block");
		itemMeta.setLore(Arrays.asList("Place within enemy", "territory to create", "bridges and staircases"));
		siegeBlockItem.setItemMeta(itemMeta);

		ShapedRecipe siegeBlock = new ShapedRecipe(siegeBlockItem);
		siegeBlock.shape("AAA", "ABA", "AAA");
		siegeBlock.setIngredient('A', Material.REDSTONE);
		siegeBlock.setIngredient('B', Material.GOLD_INGOT);
		getServer().addRecipe(siegeBlock);
	}
}
