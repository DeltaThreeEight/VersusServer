import Server.Server;
import IOStuff.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        String file = "E:\\file.txt";


        //Запуск нового потока для записи в файл при перехвате сигнала завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (args.length == 0) MyReadWriter.writeFile(file);
            } catch (Exception e) {
                System.err.println("Запись в файл не удалась.");
            }
        }));

        if (args.length == 0) {
            try {
                if (MyReadWriter.readFile(file)) {
                    System.out.println("Файл успешно прочитан");

                    Server server = new Server();
                    server.start();

                    boolean exit = false;
                    while (!exit) {
                        exit = MyReadWriter.readCommand(file);
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

