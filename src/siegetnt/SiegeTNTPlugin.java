package siegetnt;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import siegetnt.listener.ExplosionListener;
import siegetnt.listener.PlayerActionListener;
import siegetnt.listener.SiegeBlockListener;
import siegetnt.listener.WorldListener;

import java.util.Iterator;
import java.util.logging.Logger;

public class SiegeTNTPlugin extends JavaPlugin {

	private final ShockRadiusTracker shockRadiusTracker = new ShockRadiusTracker();

	@Override
	public void onEnable() {
		PluginManager manager = this.getServer().getPluginManager();
		manager.registerEvents(new ExplosionListener(shockRadiusTracker), this);
		manager.registerEvents(new WorldListener(shockRadiusTracker), this);
		manager.registerEvents(new PlayerActionListener(), this);
		manager.registerEvents(new SiegeBlockListener(this), this);

		replaceSiegeBlockRecipe();
		Logger.getLogger("Minecraft").info("SiegeTNT enabled");
	}

	private void replaceSiegeBlockRecipe() {
		Iterator<Recipe> it = getServer().recipeIterator();
		Recipe recipe;
		while (it.hasNext()) {
			recipe = it.next();
			if (recipe != null && recipe.getResult().getType() == Material.REDSTONE_BLOCK) {
				it.remove();
			}
		}

		ItemStack redstoneBlock = new ItemStack(Material.REDSTONE_BLOCK, 2);
		ShapedRecipe siegeBlock = new ShapedRecipe(redstoneBlock);
		siegeBlock.shape("ABA", "BAB", "ABA");
		siegeBlock.setIngredient('A', Material.REDSTONE);
		siegeBlock.setIngredient('B', Material.GOLD_INGOT);
		getServer().addRecipe(siegeBlock);
	}
}
