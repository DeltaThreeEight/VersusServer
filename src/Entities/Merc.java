package Entities;

import World.Location;

import java.util.Date;

public class Merc extends Human {

    transient private int ammo = 60;

    {
        setHealth(150);
    }

    public Merc(String name) {
        super(name);
        getLocation().setXY(90,90);
    }

    public Merc(String name, Location location) {
        super(name, location);
    }

    public Merc(String name, Location location, Date date) {
        super(name, location, date);
    }

    public void shoot() {
        //TODO как же будет стрелять винтовка?
    }

}
