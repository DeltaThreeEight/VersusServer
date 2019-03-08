package Entities;

import World.Location;

import java.util.Date;

public class Spy extends Human {

    {
        setSpeedModifier(1.5);
    }

    public Spy(String name) {
        super(name);
        getLocation().setXY(10,10);
    }

    public Spy(String name, Location location) {
        super(name, location);
    }

    public Spy(String name, Location location, Date date) {
        super(name, location, date);
    }

    public void shoot() {
        //TODO придумать как же стерлять из шокера
    }
}
