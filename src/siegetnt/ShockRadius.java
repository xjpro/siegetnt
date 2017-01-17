package siegetnt;

import org.bukkit.Location;

/**
 *
 * @author Me
 */
public class ShockRadius {

    public final static int RADIUS = 12;

    private final Location centerLocation;

    public ShockRadius(Location epicenter) {
        this.centerLocation = epicenter;
    }

    public double distance(Location anotherLocation) {
        return centerLocation.distance(anotherLocation);
    }

    public boolean isInRadius(Location anotherLocation) {
        return distance(anotherLocation) < RADIUS;
    }
}
