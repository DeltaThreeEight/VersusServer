package Entities;

import Exceptions.NotAliveException;
import Server.Commands.ClientCommand;
import World.Location;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.io.IOException;
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

    // Чисто клиентские поля
    private Rectangle col_rec;
    protected Ellipse body;
    protected Ellipse head;
    protected Ellipse right_hand;
    protected Ellipse left_hand;
    protected Rectangle right_arm;
    protected Rectangle left_arm;
    protected Rectangle gun;
    protected Pane root;

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

    public void teleportOther(double x, double y) {
        setTranslateX(x);
        setTranslateY(y);
        loc.setXY(x, y);
    }

    public void setLastMove(Moves move) {
        lastMove = move;
    }

    public void move(Moves move) throws NotAliveException {

        if (isAlive()) {
            lastMove = move;

            setTranslateY(getTranslateY() + move.getY() * speedModifier);
            setTranslateX(getTranslateX() + move.getX() * speedModifier);

            loc.setXY(loc.getX() + move.getX() * speedModifier, loc.getY() + move.getY() * speedModifier);

            System.out.println("Перемещение " + loc);
        } else System.out.println("Перемещение невозможно");
    }

    public void hit() {
        setHealth(hp - 10);
    }

    public int getAmmo() {
        return ammo;
    }

    public void reload() {
        ammo = 30;
    }

    public void shoot() {
        ammo--;
    }

    public Location getLocation() {
        return loc;
    }

    public void setSpeedModifier(Double mod) {
        speedModifier = mod;
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
        Human human = (Human) o;
        return hp == human.hp &&
                Double.compare(human.speedModifier, speedModifier) == 0 &&
                Objects.equals(name, human.name) &&
                Objects.equals(loc, human.loc) &&
                lastMove == human.lastMove &&
                Objects.equals(dateOfCreation, human.dateOfCreation) &&
                Objects.equals(user, human.user);
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


    // Чисто клиентские методы, нужны только для совместимости объектов
    public Moves getLastMove() {
        return lastMove;
    }
    public Rectangle getCollisionBox() {
        return col_rec;
    }
    public void setCollisionBox(Rectangle col_rec) {
        this.col_rec = col_rec;
    }
    public void setHandler(Object handler) { }
    public void show() { }
    public void show(String res) { }
    public void hide() { }
    public void moveOther(Moves move) { }
    public void teleport(double x, double y) { }
    public boolean checkIntersects(Moves move) { return false; }
    public void rotare(boolean b) { }
    public void shootOther() { }
    public int getHealth() {
        return hp;
    }
    public void shootAnim() { }
    public void died(Human human) { }
    public double getSpeedModifier() {
        return speedModifier;
    }

}
