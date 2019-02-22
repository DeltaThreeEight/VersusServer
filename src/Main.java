import World.WorldManager;
import IOStuff.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {


        String file = "E:\\file.txt";

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (args.length != 0) MyReadWriter.writeFile(args[0]);
            } catch (Exception e) {
                System.out.println("Запись в файл не удалась.");
            }
        }));

        boolean exit = false;
        if (args.length != 0) {
            try {
                if (MyReadWriter.readFile(args[0])) {
                    System.out.println("Файл успешно прочитан");
                    MyReadWriter.help();
                    while (!exit)
                        exit = MyReadWriter.readCommand(args[0]);
                }
                else
                    System.out.println("Ошибка парсинга. Попробуйте другой файл.");
            } catch (IOException e) {
                System.out.println("Нет доступа к файлу или файл не существует.");
            }
        } else System.out.println("Не введено имя файла.");

    }
}
