package Entities;

import World.Locations;
import World.WorldManager;

import java.util.Objects;

public class Human extends Creature {

    transient private State state = State.NORMAL;

    public Human(String iName, Locations iLocation) {
        super(iName,iLocation);
    }

    public void changeState(State iState,String msg) {
            checkAlive();
            System.out.println(getName() + msg);
            this.state = iState;
    }

    //Служебный метод, не рекоммендуется использововать
    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Human)) return false;
        if (!super.equals(o)) return false;
        Human human = (Human) o;
        return state == human.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), state);
    }
}
