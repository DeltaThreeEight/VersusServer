package Entities;

import Exceptions.NotAliveException;
import World.Location;

public interface Moveable {
    void move(Moves move) throws NotAliveException;
    Location getLocation();
    double distance(Moveable moveable);
}
