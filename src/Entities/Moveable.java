package Entities;

import Exceptions.*;
import World.*;

public interface Moveable {
    void move(Locations iLocation) throws MoveException, NotAliveException;
    Locations getLocation();
}
