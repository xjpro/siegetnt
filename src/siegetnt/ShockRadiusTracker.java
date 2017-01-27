package siegetnt;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ShockRadiusTracker {

    private final List<ShockRadius> shockRadiuses = new ArrayList<>();

    public void addShockRadiusLocation(Location loc) {
        ShockRadius shockRadius = new ShockRadius(loc);
        shockRadiuses.add(shockRadius);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("SiegeTNT"), () -> {
            shockRadiuses.remove(shockRadius);
        }, 600); // 1200 = 1 minute
    }

    public boolean isInShockLocation(Location loc) {
        for (ShockRadius radius : shockRadiuses) {
            if (radius.isInRadius(loc)) {
                return true;
            }
        }
        return false;
    }
}
