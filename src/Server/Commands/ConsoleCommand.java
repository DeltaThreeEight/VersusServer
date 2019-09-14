package Server.Commands;

import Server.Server;

public class ConsoleCommand implements Command {
    private String commandName;
    private String[] args;

    public ConsoleCommand(String name, String ...args) {
        this.commandName = name;
        this.args = args;
    }

    public String getArg(int argument) {
        return args[argument];
    }

    public int getArgsCount() {
        return args.length;
    }

    public String getArgsAsOne() {
        StringBuilder builder = new StringBuilder();
        for (String s : args)
            builder.append(s + " ");

        return builder.toString();
    }

    public String getCommandName() {
        return commandName;
    }
}
