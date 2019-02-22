package Entities;

public enum State {
    HAPPY("счастлив"),
    SAD("грустит"),
    NORMAL("в обычном состоянии"),
    SCARED("очень напуган");
    State(String s) {
        ru = s;
    }
    private String ru;
    public String toString() {
        return ru;
    }
}
