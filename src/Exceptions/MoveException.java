package Exceptions;

import Entities.Moveable;

public class MoveException extends Exception {//Checked exception

    private Moveable moveable; //Объект пытавшийся переместиться

    public MoveException(Moveable moveable) {
        super("Неизвестная ошибка перемещения");
        this.moveable = moveable;
    }
    public MoveException(Moveable moveable, String s) {
        super(s);
        this.moveable = moveable;
    }

    public Moveable getMoveable() {
        return moveable;
    }
}
