package siegetnt;

import java.util.logging.Logger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import siegetnt.listener.*;

public class SiegeTNTPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PluginManager manager = this.getServer().getPluginManager();

        manager.registerEvents(new ExplosionListener(), this);
        manager.registerEvents(new WorldListener(), this);

        Logger.getLogger("Minecraft").info("SiegeTNT enabled");
    }
}
