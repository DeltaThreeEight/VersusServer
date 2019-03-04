package Entities;

import Exceptions.MoveException;
import Exceptions.NotAliveException;
import World.Location;

public interface Moveable {
    void move(Moves move) throws MoveException, NotAliveException;
    Location getLocation();
}
