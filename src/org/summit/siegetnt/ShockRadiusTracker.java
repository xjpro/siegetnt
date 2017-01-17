
package org.summit.siegetnt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 *
 * @author Me
 */
public abstract class ShockRadiusTracker
{
    private static class ShockRadiusRemover implements Runnable
    {
        private final ShockRadius shockRadius;
        public ShockRadiusRemover(ShockRadius toBeRemoved)
        {
            shockRadius = toBeRemoved;
        }
        
        @Override
        public void run() 
        {
            ShockRadiusTracker.removeShockRadiusLocation(shockRadius);
        }
    }
    
    private static List<ShockRadius> locations =  new ArrayList<ShockRadius>();
    
    public static Collection<ShockRadius> getShockRadiusLocations()
    {
        return locations;
    }
    
    public static void addShockRadiusLocation(Location loc)
    {
        ShockRadius shockRadius = new ShockRadius(loc);
        locations.add(shockRadius);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("SiegeTNT"), new ShockRadiusRemover(shockRadius), 600);
        // 1200 = 1 minute
    }
    
    public static void removeShockRadiusLocation(ShockRadius shockRadius)
    {
        locations.remove(shockRadius);
    }
    
    public static boolean isInShockLocation(Location loc)
    {
        for(ShockRadius radius : locations)
        {
            if(radius.isInRadius(loc)) return true;
        }
        return false;
    }
}
