package siegetnt;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import siegetnt.listener.*;

public class SiegeTNTPlugin extends JavaPlugin {

    private final ShockRadiusTracker shockRadiusTracker = new ShockRadiusTracker();

    @Override
    public void onEnable() {
        PluginManager manager = this.getServer().getPluginManager();

        manager.registerEvents(new ExplosionListener(shockRadiusTracker), this);
        manager.registerEvents(new WorldListener(shockRadiusTracker), this);
        manager.registerEvents(new PlayerActionListener(), this);
        manager.registerEvents(new SiegeBlockListener(this), this);

        Logger.getLogger("Minecraft").info("SiegeTNT enabled");
    }
}
