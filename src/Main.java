import Server.Server;
import IOStuff.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        String file = "E:\\file.txt";


        //Запуск нового потока для записи в файл при перехвате сигнала завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (args.length != 0) MyReadWriter.writeFile(args[0]);
            } catch (Exception e) {
                System.err.println("Запись в файл не удалась.");
            }
        }));

        if (args.length != 0) {
            try {
                if (MyReadWriter.readFile(args[0])) {
                    System.out.println("Файл успешно прочитан");

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

                    server.start();

                    boolean exit = false;
                    while (!exit) {
                        exit = MyReadWriter.readCommand(args[0]);
                    }
                    server.stopServer();
                } else
                    System.out.println("Ошибка парсинга. Попробуйте другой файл.");
            } catch (IOException e) {
                System.out.println("Нет доступа к файлу или файл не существует.");
            }
        } else System.out.println("Не введено имя файла.");
    }
}

