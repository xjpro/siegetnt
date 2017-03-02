package siegetnt;

import org.bukkit.Location;

public class ShockRadius {

	private final static int RADIUS = 12;

	private final Location epicenter;

	public ShockRadius(Location epicenter) {
		this.epicenter = epicenter;
	}

	public boolean isInRadius(Location location) {
		return distanceIgnoreY(location) <= RADIUS;
	}

	private double distanceIgnoreY(Location other) {
		return Math.sqrt(Math.pow(epicenter.getX() - other.getX(), 2) + Math.pow(epicenter.getZ() - other.getZ(), 2));
	}
}
