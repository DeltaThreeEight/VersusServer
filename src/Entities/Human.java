package Entities;

import Exceptions.NotAliveException;
import World.Location;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public abstract class Human implements Moveable, Comparable<Human>, Serializable {

    private String name;
    private Location loc;
    private int hp = 100;
    private Moves lastMove = Moves.BACK;
    private Date dateOfCreation;
    private double speedModifier = 1.0;

    public Human(String iName) {
        this.name = iName;
        this.loc = new Location(0, 0);
        dateOfCreation = new Date();
    }

    public Human(String iName, Location iLoc) {
        this.name = iName;
        this.loc = iLoc;
        dateOfCreation = new Date();
    }

    public Human(String iName, Location iLoc, Date date) {
        this.name = iName;
        this.loc = iLoc;
        dateOfCreation = date;
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
        return name + getClass().toString().replace("class Entities.", " ") + " "+ loc;
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

    public Date getDate() {
        return dateOfCreation;
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

    public double distance(Moveable moveable) {
        return Math.sqrt(Math.pow(getLocation().getY()-moveable.getLocation().getY(), 2.0)
                + Math.pow((getLocation().getX()-moveable.getLocation().getX()), 2.0));
    }

}
