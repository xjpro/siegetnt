package siegetnt;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShockRadiusTracker {

	public static final int SHOCK_RADIUS_SECONDS = 600;
	private final List<ShockRadius> shockRadiuses = new ArrayList<>();

	public void addShockRadiusLocation(Location loc) {
		ShockRadius shockRadius = new ShockRadius(loc);
		shockRadiuses.add(shockRadius);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("SiegeTNT"), () -> shockRadiuses.remove(shockRadius), SHOCK_RADIUS_SECONDS * 20);
	}

	public ShockRadius getLatestShockRadius(Location location) {
		return shockRadiuses.stream()
				.filter(radius -> radius.isInRadius(location))
				.sorted(Comparator.comparing(ShockRadius::getCreated))
				.reduce((a, b) -> b) // get last
				.orElse(null);
	}
}
