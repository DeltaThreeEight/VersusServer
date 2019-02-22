package Entities;

public enum Animals {
    DOG("Собака"),
    PIG("Свинка"),
    DUCK("Утка"),
    BEAR("Мишка"),
    CAT("Кошка"),
    WOLF("Волк"),
    SHEEP("Овечка"),
    PENGUIN("Пингвин");
    Animals(String s) {
        name = s;
    }
    String name;
    public String toString() {
        return name;
    }
}
