package Exceptions;

import Server.Commands.Command;

public class CommandExecuteException extends RuntimeException {

    private Command command;

    public CommandExecuteException(Command command, String cause) {
        super(cause);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

}
