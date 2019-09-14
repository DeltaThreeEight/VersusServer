import Server.*;
import Server.Commands.ConsoleCommand;

public class Main {

    public static void main(String[] args) {

        ServerConsole serverConsole = ServerConsole.getInstance();

        int port = 21326;
        String host = "localhost";

        try {
            switch (args.length) {
                case 1:
                    port = Integer.parseInt(args[0]);
                    break;
                case 2:
                    port = Integer.parseInt(args[0]);
                    host = args[1];
            }
        } catch (Exception e) {
            System.err.printf("Неверно задан порт/хост: %s", e.getMessage());
        }

        serverConsole.startServer(host, port);

        boolean exit = false;
        while (!exit) {
            ConsoleCommand command = serverConsole.readCommand();
            exit = serverConsole.executeCommand(command);
        }

        serverConsole.stopServer();

    }
}

