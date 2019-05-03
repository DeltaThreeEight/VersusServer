package Entities;

import Exceptions.NotAliveException;
import World.Location;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Human extends FlowPane implements Moveable, Comparable<Human>, Serializable {
    private String name;
    private Location loc;
    private int hp = 100;
    private int ammo = 30;
    private Moves lastMove = Moves.BACK;
    private LocalDateTime dateOfCreation;
    private double speedModifier = 1.0;
    private String user = "default";
    protected Ellipse body;
    protected Ellipse head;
    protected Ellipse right_hand;
    protected Ellipse left_hand;
    protected Rectangle right_arm;
    protected Rectangle left_arm;
    protected Rectangle gun;
    private Rectangle col_rec;
    protected Pane root;

    public Moves getLastMove() {
        return lastMove;
    }

    public Rectangle getCol_rec() {
        return col_rec;
    }

    public void setCol_rec(Rectangle col_rec) {
        this.col_rec = col_rec;
    }

    public void setLastMove(Moves move) {
        lastMove = move;
    }

    public static void kill(double x, double y) {}

    public void rotare(boolean b) {}

    public void setUser(String str) {
        user = str;
    }

    public String getUser() {
        return user;
    }

    public Human(String iName) {
        this.name = iName;
        this.loc = new Location(0, 0);
        dateOfCreation = LocalDateTime.now();
    }

    public int getAmmo() {
        return ammo;
    }

    public void reload() {
        ammo = 30;
    }

    public Human(String iName, Location iLoc) {
        this.name = iName;
        this.loc = iLoc;
        dateOfCreation = LocalDateTime.now();
    }

    public Human(String iName, Location iLoc, LocalDateTime date) {
        this.name = iName;
        this.loc = iLoc;
        dateOfCreation = date;
        setTranslateY(loc.getY());
        setTranslateX(loc.getX());
    }

    public void show() {}


    public void show(String res) {}
    public void hide() {}

    public boolean checkIntersects(Moves move) {return false;}

    public void teleport(double x, double y) {}

    public void teleportOther(double x, double y) {
        setTranslateX(x);
        setTranslateY(y);
        loc.setXY(x, y);
    }

    public void moveOther(Moves move) {}

    public void move(Moves move) throws NotAliveException {

        if (isAlive()) {
            lastMove = move;

            setTranslateY(getTranslateY() + move.getY() * speedModifier);
            setTranslateX(getTranslateX() + move.getX() * speedModifier);

            loc.setXY(loc.getX() + move.getX() * speedModifier, loc.getY() + move.getY() * speedModifier);

            System.out.println("Перемещение " + loc);
        } else System.out.println("Перемещение невозможно");
    }

    public void shootOther() {

    }

    public void shootAnim() {}

    public void shoot() {
        ammo--;
    }

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

    public boolean isAlive() {
        return hp > 0 ? true : false;
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

    public LocalDateTime getDate() {
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