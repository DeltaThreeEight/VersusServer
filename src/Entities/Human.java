package Entities;

import Exceptions.NotAliveException;
import World.Location;

import java.util.Objects;

public abstract class Human implements Moveable, Comparable<Human>{

    private String name;
    private Location loc;
    transient private int hp = 100;
    transient private Moves lastMove = Moves.BACK;
    transient private double speedModifier = 1.0;

    public Human(String iName) {
        this.name = iName;
        this.loc = new Location(0, 0);
    }

    public Human(String iName, Location iLoc) {
        this.name = iName;
        this.loc = iLoc;
    }

    public void move(Moves move) throws NotAliveException {
        checkAlive();
        lastMove = move;
        loc.setXY(loc.getX()+ move.getX()*speedModifier, loc.getY() + move.getY()*speedModifier);
        System.out.println(getName()+" перемещается в "+loc.getName());
    }

    public abstract void shoot();

    public int getHealth() {
        return hp;
    }

    public Location getLocation() {
        return loc;
    }

    public double getSpeedModifier() {
        return speedModifier;
    }

    public void setSpeedModifier(Double mod) {
        speedModifier = mod;
    }

    public void died(Human human) {
        hp = 0;
        System.out.println(name + " был убит " + human.getName() + "ом");
    }

    public String getName() {
        return name;
    }

    public void checkAlive() throws NotAliveException{
        if (hp < 1) throw new NotAliveException(this);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Human)) return false;
        Human creature = (Human) o;
        return Objects.equals(name, creature.name) &&
                Objects.equals(loc, creature.loc) &&
                Objects.equals(hp, creature.hp);
    }

    public int compareTo(Human human) {
        return human.name.length() - name.length();
    }

    public void setHealth(int health) {
        hp = health;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, loc, hp);
    }

}
