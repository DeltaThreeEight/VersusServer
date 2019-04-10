import Server.*;
import IOStuff.*;

public class Main {

    public static void main(String[] args) {

        MyReadWriter myReadWriter = MyReadWriter.getInstance();

                    Server server = null;

                    try {
                        switch (args.length) {
                            case 1:
                                server = new Server(Integer.parseInt(args[0]));
                                break;
                            case 2:
                                server = new Server(Integer.parseInt(args[0]), args[1]);
                                break;
                            default:
                                server = new Server();
                        }

                    } catch (Exception e) {
                        System.out.println("Неверно задан порт/хост");
                        System.exit(-1);
                    }
                    myReadWriter.setServer(server);
                    server.start();

                    boolean exit = false;
                    while (!exit) {
                        exit = myReadWriter.readCommand();
                    }
                    server.stopServer();
    }
}

