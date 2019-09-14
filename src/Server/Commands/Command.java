package Server.Commands;

import Server.Server;

public interface Command {
    String getCommandName();
    String getArg(int i);
    int getArgsCount();
    String getArgsAsOne();
}
