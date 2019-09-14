package Server.Commands;

import Entities.Human;
import java.io.Serializable;

public class ClientCommand implements Command, Serializable {
    private String name;
    private String args[];
    private String token;
    private Human person;

    public ClientCommand(String name) {
        this.name = name;
        this.args = null;
    }

    public ClientCommand(String name, Human person) {
        this.name = name;
        this.args = null;
        this.person = person;
    }

    public ClientCommand(String name, String ... args) {
        this.name = name;
        this.args = args;
    }


    public Human getPerson() {
        return person;
    }

    public String getCommandName() {
        return name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getArg(int i) {
        return args[i];
    }

    public String getArgsAsOne() {
        StringBuilder builder = new StringBuilder();
        for (String s : args)
            builder.append(s + " ");

        return builder.toString();
    }

    public int getArgsCount() {
        return args.length;
    }

}
