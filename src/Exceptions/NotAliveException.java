package Exceptions;

import Entities.Human;

public class NotAliveException extends RuntimeException {//Unchecked exception
    private Human human; //Объект пытавшийся переместиться

    public NotAliveException(Human human) {
        super(human.getName() + " мертв.");
        this.human = human;
    }

    public Human getNotAliveHuman() {
        return human;
    }
}
