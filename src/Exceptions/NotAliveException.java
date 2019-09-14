package Exceptions;

import Entities.Human;

public class NotAliveException extends RuntimeException {
    private Human human;

    public NotAliveException(Human human) {
        super(human.getName() + " мертв.");
        this.human = human;
    }

    public Human getNotAliveHuman() {
        return human;
    }
}
