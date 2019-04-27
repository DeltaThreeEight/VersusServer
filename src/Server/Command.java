package Server;

import java.io.Serializable;

public class Command implements Serializable {
    private String name;
    private String args[];
    private String token;

    public Command(String name, String ... args) {
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String[] getArgs() {
        return args;
    }
}
