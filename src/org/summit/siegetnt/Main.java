package org.summit.siegetnt;

import java.util.logging.Logger;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.summit.siegetnt.listener.*;

public class Main extends JavaPlugin 
{
    private static Main instance = null;

    @Override
	public void onEnable()
	{
		PluginManager manager = this.getServer().getPluginManager();

        manager.registerEvents(new ExplosionListener(), this);
        manager.registerEvents(new WorldListener(), this);

        Logger.getLogger("Minecraft").info("SiegeTNT enabled");
        instance = this;
	}
	
    @Override
	public void onDisable()
	{
	}
    
    public static Configuration config() 
    {
        return instance.getConfig();
    }

}
