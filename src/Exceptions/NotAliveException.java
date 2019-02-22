package Exceptions;

import Entities.Creature;

public class NotAliveException extends RuntimeException {//Unchecked exception
    private Creature creature; //Объект пытавшийся переместиться

    public NotAliveException(Creature creature) {
        super(creature.getName() + " мертв.");
        this.creature = creature;
    }

    public Creature getNotAliveCreature() {
        return creature;
    }
}
