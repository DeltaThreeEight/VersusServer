package Entities;

import Exceptions.*;
import World.*;

public interface Moveable {
    void move(Moves move) throws MoveException, NotAliveException;
    Locations getLocation();
}
