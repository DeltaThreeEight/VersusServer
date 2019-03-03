package Entities;

import Exceptions.*;
import World.*;
import java.util.Objects;

public abstract class Creature implements Moveable, Comparable<Creature> {
    private String name;
    private Locations loc;
    transient private boolean isWet;
    transient private boolean isAlive = true;

    public Creature(String iName, Locations iLocation) {
        this.name = iName;
        this.loc = iLocation;
    }

    public void move(Moves move) throws MoveException, NotAliveException {
        checkAlive();
        loc.setXY((int) loc.getX()+ move.getX(), (int) loc.getY() + move.getY());
        if (WorldManager.getWeather() == Weather.RAIN) this.setWet(true);
        else this.setWet(false);
        System.out.println(getName()+" перемещается в "+loc.getName());
    }

    public Locations getLocation() {
        return loc;
    }

    public void died(Creature creature) {
        isAlive = false;
        System.out.println(name + " был убит " + creature.getName() + "ом");
    }

    public String getName() {
        return name;
    }

    public void setWet(boolean i) {
        if (!loc.getBuilding() && isWet != i) {
            isWet = i;
            System.out.println(name + (isWet ? " промок" : " высох"));
        }
    }

    public void checkAlive() throws NotAliveException{
        if (isAlive == false) throw new NotAliveException(this);
    }

    public boolean getWet() {
        return isWet;
    }
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Creature)) return false;
        Creature creature = (Creature) o;
        return Objects.equals(name, creature.name) &&
                Objects.equals(loc, creature.loc) &&
                Objects.equals(isAlive, creature.isAlive);
    }

    public int compareTo(Creature creature) {
        return creature.name.length() - name.length();
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, loc, isAlive);
    }
}
