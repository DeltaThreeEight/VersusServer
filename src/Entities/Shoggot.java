package Entities;

import Exceptions.MoveException;
import World.Locations;
import World.WorldManager;

public class Shoggot extends Creature {

    public Shoggot(String iName, Locations iLocation) {
        super(iName, iLocation);
    }

    public void attack(Human iHuman) {
        checkAlive();
        iHuman.checkAlive();
        if (getLocation().equals(iHuman.getLocation())) {
            System.out.println("Текеле-ли! Текеле-ли! "+iHuman.getName()+" в ужасе пытается спастись от шоггота "+getName());
            iHuman.died(this);
        } else {
            System.out.println(iHuman.getName()+" в другой локации.");
        }
    }

}
