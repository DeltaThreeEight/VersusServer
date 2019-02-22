package World;

public class Locations {

    private double x,y;
    private String name;
    private boolean isBuilding;

    public Locations(double iX,double iY, String iName,boolean iBuilding) {
        this.x = iX;
        this.y = iY;
        this.isBuilding = iBuilding;
        this.name = iName;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean getBuilding() {
        return isBuilding;
    }

}
