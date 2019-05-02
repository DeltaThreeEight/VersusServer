package Entities;

import World.Location;

import java.time.LocalDateTime;

public class Spy extends Human {

    {
        setSpeedModifier(1.5);
    }

    public Spy(String name) {
        super(name);
        getLocation().setXY(10,10);
    }

    public void show() {}

    public Spy(String name, Location location) {
        super(name, location);
    }

    public Spy(String name, Location location, LocalDateTime date) {
        super(name, location, date);
    }

    public void shoot() {
        super.shoot();
    }
}
