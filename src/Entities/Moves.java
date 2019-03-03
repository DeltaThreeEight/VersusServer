package Entities;

public enum Moves {
    LEFT(-1,0),
    RIGHT(1,0),
    FORWARD(0,1),
    BACK(0,-1);
    private int x,y;
    Moves(int x,int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
