package World;

public enum Weather {
    SUNNY("солнечно"),
    RAIN("дождь");
    Weather(String s) {
        name = s;
    }
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
