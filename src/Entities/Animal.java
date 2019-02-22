package Entities;

import World.*;
import java.util.Objects;

public class Animal extends Creature {
    private Animals kindOfAnimal;

    public Animal(String iName, Locations iLocation) {
        super(iName, iLocation);
        kindOfAnimal = Animals.DOG;
    }

    public Animal(String iName,Animals iKind,Locations iLocation) {
        super(iName, iLocation);
        kindOfAnimal = iKind;
    }

    public Animals getKindOfAnimal() {
        return kindOfAnimal;
    }


    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animal)) return false;
        if (!super.equals(o)) return false;
        Animal animal = (Animal) o;
        return kindOfAnimal == animal.kindOfAnimal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), kindOfAnimal);
    }
}
