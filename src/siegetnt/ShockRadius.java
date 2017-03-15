package siegetnt;

import org.bukkit.Location;

import java.util.Date;

public class ShockRadius {

	private final static int RADIUS = 12;

	private final Date created;
	private final Location epicenter;

	ShockRadius(Location epicenter) {
		this.created = new Date();
		this.epicenter = epicenter;
	}

	public Date getCreated() {
		return created;
	}

	boolean isInRadius(Location location) {
		return distanceIgnoreY(location) <= RADIUS;
	}

	private double distanceIgnoreY(Location other) {
		return Math.sqrt(Math.pow(epicenter.getX() - other.getX(), 2) + Math.pow(epicenter.getZ() - other.getZ(), 2));
	}
}
