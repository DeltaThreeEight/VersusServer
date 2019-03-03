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

    public Locations(double iX,double iY) {
        this.x = iX;
        this.y = iY;
        this.isBuilding = false;
        this.name = "Текущее местоположение";
    }

    public void setXY(int x, int y) {
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

    public boolean getBuilding() {
        return isBuilding;
    }

}
