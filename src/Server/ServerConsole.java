package Server;

import Entities.Human;
import Exceptions.CommandExecuteException;
import Server.Commands.ConsoleCommand;
import World.WorldManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <pre>
 * Класс управляющий записью/чтением файлов, а также управляющий пользовательским вводом.
 *
 * Команды при работе в интерактивном режиме:
 *
 * <b>insert</b> <i>{string}</i> <i>{element}</i> - добавить новый элемент с заданным ключом, {element} должен быть в формате json, {string} - строка без пробелов
 * Пример:
 * <code>
 *     insert ExampleKey {
 *          "type": "Animal",
 *          "kindOfAnimal":"DOG",
 *          "name":"Эффа",
 *          "loc":
 *              {
 *              "x":10.0,
 *              "y":30.0,
 *              "name":"Будка",
 *              "isBuilding":true
 *              }
 *          }
 * </code>
 * Вместо Animal могут быть два других типа: Human и Shoggot;
 * Поле kindOfAnimal может принимать следующие значения: DOG, PIG, DUCK, BEAR, CAT, WOLF, SHEEP, PENGUIN.
 * Поле isBuilding может быть не указано, тогда будет считаться, что его значение - false
 *
 * <b>show</b> - вывести список элементов коллекции.
 *
 * <b>clear</b> - удалить из коллекции все элементы.
 *
 * <b>info</b> - вывести информацию о коллекции
 *
 * <b>stop</b> - остановить сервер</pre>
 */
public class ServerConsole {

    private final Gson gson = new GsonBuilder().registerTypeAdapter(Human.class, new GsonController()).create();
    private static ServerConsole instance = null;
    private WorldManager world;
    private Server server;
    private Scanner scanner = new Scanner(System.in);
    private Logger consoleLogger = Logger.getLogger("Main logger");
    private Thread serverThread;


    private ServerConsole() { }

    public static ServerConsole getInstance() {
        if (instance == null) {
            instance = new ServerConsole();
            return instance;
        } else return instance;
    }

    public void startServer(String host, int port) {
        world = new WorldManager();

        this.server = new Server(host, port, world);
        serverThread = new Thread(server, "Server Thread");

        consoleLogger.log(Level.INFO, "Начинаем запуск сервера.");
        serverThread.start();
    }

    public void stopServer() {
        consoleLogger.log(Level.INFO, "Остановка сервера...");
        server.stopServer();
    }

    public ConsoleCommand readCommand() {
        String[] command = scanner.nextLine().split("\\s+");

        String commandName = command[0];
        String[] args = Arrays.copyOfRange(command, 1, command.length);

        return new ConsoleCommand(commandName, args);
    }

    public boolean executeCommand(ConsoleCommand command) {
        try {
            consoleLogger.log(Level.INFO, "Попытка вызвать команду " + command.getCommandName());
            switch(command.getCommandName().trim()) {
                case "insert":
                    if (command.getArgsCount() < 1)
                        throw new CommandExecuteException(command, "Недостаточно аргументов для команды %s\n");

                    if (server.hasPlayers())
                        throw new CommandExecuteException(command, "Команда %s не поддерживается, когда на сервере есть игроки\n");

                    //Реализация инсерта..
                    //А может её вообще не надо?
                    break;
                case "show":
                    world.showHumans();
                    break;
                case "clear":
                    if (server.hasPlayers())
                        throw new CommandExecuteException(command, "Команда %s не поддерживается, когда на сервере есть игроки\n");

                    world.clear();
                    break;
                case "save":
                    server.getDBC().updatePersons();
                    break;
                case "info":
                    world.getInfo();
                    break;
                case "help":
                    help();
                    break;
                case "send":
                    if (command.getArgsCount() < 1)
                        throw new CommandExecuteException(command, "Отсутсвуют аргументы команды %s\n");

                    server.sendToAllClients(command.getArgsAsOne(), null);

                    System.out.printf("Сообщение \"%s\" успешно отправлено.\n", command.getArgsAsOne());

                    break;
                case "stop":
                    scanner.close();
                    return true;
                case "":
                    break;
                default:
                    System.out.printf("Команда %s не найдена\n", command.getCommandName());
            }
            return false;
        } catch (CommandExecuteException e) {
            System.out.printf(e.getMessage(), e.getCommand().getCommandName());
        } catch (NoSuchElementException e) {
            consoleLogger.log(Level.WARNING, "Получен сигнал завершения работы программы.");
            return true;
        } catch (Exception e) {
            System.err.printf("Ошибка парсинга: %s\n", e.getMessage());
            return false;
        }
        return false;
    }

//    private Human startParsing(Scanner scanner, String iJson) {
//        boolean flag = true;
//        StringBuilder json = new StringBuilder("" + iJson);
//
//        if (((json.length()- json.toString().replaceAll("\\{","").length()) - (json.length()- json.toString().replaceAll("}","").length()))<1) flag = false;
//
//        while (flag) {
//            json.append(scanner.nextLine());
//            if (((json.length()- json.toString().replaceAll("\\{","").length()) - (json.length()- json.toString().replaceAll("}","").length()))<1) flag = false;
//        }
//        try {
//            return gson.fromJson(json.toString(), Human.class);
//        } catch (Exception ignored) {
//        }
//        return null;
//    }


    private void help() {
        System.out.print("Команды при работе в интерактивном режиме:\n\n" +
                "insert {string} {element} - добавить новый элемент с заданным ключом, {element} должен быть в формате json,а String - строка без пробельных символов\n" +
                "Пример:\n" +
                "insert ExampleKey {\n" +
                "   \"side\": \"Spy\",\n" +
                "   \"name\":\"Шпиён\",\n" +
                "   \"loc\":\n" +
                "       {\n" +
                "       \"x\":10.0,\n" +
                "       \"y\":30.0,\n" +
                "       }\n" +
                "}\n" +
                "Вместо Spy может быть Merc\n\n" +
                "show - вывести список элементов коллекции.\n\n" +
                "help - помощь.\n\n" +
                "info - вывести информацию о коллекции.\n\n" +
                "remove {string} - удалить элемент из коллекции по его ключу.\n\n" +
                "stop - остановить сервер.\n");
    }
}