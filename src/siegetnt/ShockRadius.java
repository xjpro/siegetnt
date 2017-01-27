package siegetnt;

import org.bukkit.Location;

public class ShockRadius {

    public final static int RADIUS = 12;

    private final Location center;

    public ShockRadius(Location epicenter) {
        this.center = epicenter;
    }

    public double distance(Location anotherLocation) {
        return center.distance(anotherLocation);
    }

    public boolean isInRadius(Location anotherLocation) {
        return distance(anotherLocation) < RADIUS;
    }
}
