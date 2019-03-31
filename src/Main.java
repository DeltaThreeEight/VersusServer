import Server.Server;
import IOStuff.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        MyReadWriter myReadWriter = MyReadWriter.getInstance();

        String file = "E:\\file.txt";


        //Запуск нового потока для записи в файл при перехвате сигнала завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (args.length != 0) myReadWriter.writeFile(args[0]);
        }));

        if (args.length != 0) {
                    myReadWriter.readFile(args[0]);

                    Server server = null;

                    try {
                        switch (args.length) {
                            case 2:
                                server = new Server(Integer.parseInt(args[1]));
                                break;
                            case 3:
                                server = new Server(Integer.parseInt(args[1]), args[2]);
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
                        exit = myReadWriter.readCommand(args[0]);
                    }
                    server.stopServer();
        } else System.out.println("Не введено имя файла.");
    }
}

