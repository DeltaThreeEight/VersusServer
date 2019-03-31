package World;

import java.io.Serializable;

public class Location implements Serializable {

    private double x,y;

    public Location(double iX, double iY) {
        this.x = iX;
        this.y = iY;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return getX()+" "+getY();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String toString() {
        return x + " " + y;
    }

}
