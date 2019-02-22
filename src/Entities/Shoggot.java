package Entities;

import Exceptions.MoveException;
import World.Locations;
import World.WorldManager;

public class Shoggot extends Creature {

    public Shoggot(String iName, Locations iLocation) {
        super(iName, iLocation);
        if (iLocation.getBuilding() == false) {
            try {
                this.move(new Locations(123,321,"Логово шоггота", true));
            } catch (MoveException e) {
                e.printStackTrace();
            }
        }
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

    @Override
    public void move(Locations iLocation) throws MoveException {
        checkAlive();
        if (iLocation == null) throw new MoveException(this, "Перемещение невозможно. Локация не существует.");
        if (iLocation.getBuilding() == false) throw new MoveException(this, "Перемещение невозможно. Шогготы не могут находится под открытым небом.");
        super.move(iLocation);
    }

}
