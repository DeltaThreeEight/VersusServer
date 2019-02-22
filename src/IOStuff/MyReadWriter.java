package IOStuff;

import Entities.*;
import World.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

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
 * <b>remove_greater</b> <i>{type}</i> <i>{element}</i> - удалить из коллекции все элементы длина поля Name которых, больше длины поля Name у {element}.
 *
 * <b>show</b> - вывести список элементов коллекции.
 *
 * <b>clear</b> - удалить из коллекции все элементы.
 *
 * <b>import</b> <i>{string path}</i> - добавить в коллекцию элементы из файла, где {string path) - путь к файлу.
 *
 * <b>info</b> - вывести информацию о коллекции.
 *
 * <b>remove</b> <i>{string key}</i> - удалить элемент из коллекции по его ключу.
 *
 * <b>exit</b> - выйти из программы и сохранить текущую коллекцию в файле, который был указан при запуске</pre>
 */
public class MyReadWriter {

    private static GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Human.class, new MyDeserialize());
    private static Gson gson = builder.create();
    private static boolean canRead = false;

    private MyReadWriter() {}
    /**
     * Читает файл в формате csv и заполняет коллекцию элементами
     * @param fileName путь к файлу
     * @return true, если файл успешно прочитан, false - если нетю
     * @throws IOException Может быть файл не существует или доступа к нему нет...
     */
    public static boolean readFile(String fileName) throws IOException {

        InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName));
        char c = ' ';
        int k = 0;
        while (k != -1) {
            String line = "";
            k = reader.read();
            while ((char) k != '\n') {
                line = line + (char) k;
                k = reader.read();
                if (k == -1) break;
            }
            if (line.contains(",")) {
                try {
                    Creature element = pasrseCSV(line);
                    String param[] = line.split(",");
                    if (element == null) {
                        return false;
                    }
                    WorldManager.addNewCreature(param[0], element);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }
        canRead = true;
        reader.close();
        return true;
    }

    /**
     * Записывает в файл текущее состояние коллекции.
     * @param file путь к файлу.
     * @throws IOException А что, а вдруг... доступа к файлу нет!
     */
    public static void writeFile(String file) throws IOException{
        if (canRead) {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
            for (String s : WorldManager.getCreatures().keySet()) {
                String kind = "";
                Creature c = WorldManager.getCreatures().get(s);
                if (c instanceof Animal) kind = "," + ((Animal) c).getKindOfAnimal().name();
                writer.write(s + "," + c.getClass().toString().replace("class Entities.", "") + "," + "\"" + c.getName() + "\"" +
                        "," + c.getLocation().getX() + "," + c.getLocation().getY() + "," + c.getLocation().getBuilding() + "," + "\"" + c.getLocation().getName() + "\"" + kind + "\r\n");
            }
            System.out.println("Запись в файл успешна");
            writer.close();
        }
    }

    /**
     * Функция чтения пользовательского ввода.
     * @param file путь к файлу, откуда будем читать состояние коллекции, а по завершению работы запишем его туда.
     * @return Возвращает <i>true</i> только в случае, если была введена команда <b>exit</b>.
     */
    public static boolean readCommand(String file) {
        try{
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите команду: ");
            String command = scanner.nextLine();
            String[] commands = command.split("\\s+");
            commands[0] = commands[0].toUpperCase();
            switch(commands[0]) {
                case "INSERT":
                    if (commands.length < 3) {
                        System.out.println("Отсутсвуют аргументы команды.");
                    } else {
                        if (!commands[1].equals("")) {
                            String json = "";
                            if (commands.length > 2)
                                for (int i = 2; i < commands.length; i++) {
                                    json = json + commands[i];
                                }
                            WorldManager.addNewCreature(commands[1], startParsing(scanner,json));
                            System.out.println("Элемент успешно добавлен в коллекцию.");
                        } else System.out.println("Ошибка парсинга");
                    }
                    break;
                case "REMOVE_GREATER":
                    if (commands.length < 2) {
                        System.out.println("Отсутсвуют аргументы команды.");
                    } else {
                        String json = "";
                        for (int i = 1; i < commands.length; i++) {
                            json = json + commands[i];
                        }
                        WorldManager.removeGreater(startParsing(scanner, json));
                    }
                    break;
                case "SHOW":
                    WorldManager.showCreatures();
                    break;
                case "CLEAR":
                    WorldManager.clear();
                    break;
                case "IMPORT":
                    if (commands.length < 2) {
                        System.out.println("Отсутсвуют аргументы команды.");
                    } else {
                        String path = "";
                        path = commands[1];
                        if (findString(command) != null) path = findString(command);
                        System.out.println((readFile(path)) ? "Файл успешно прочитан" : "Ошибка чтения файла");
                    }
                    break;
                case "INFO":
                    WorldManager.getInfo();
                    break;
                case "HELP":
                    help();
                    break;
                case "REMOVE":
                    if (commands.length < 2) {
                        System.out.println("Отсутсвуют аргументы команды.");
                    } else {
                        WorldManager.removeCreature(commands[1]);
                    }
                    break;
                case "EXIT":
                    scanner.close();
                    return true;
                default:
                    System.out.println("Команда не найдена");
            }
            System.out.println();
            return false;
        }
        catch (NoSuchElementException e) {
            return true;
        }
        catch (FileNotFoundException e) {
            System.out.println("Файл не найден");
            return false;
        }
        catch (NullPointerException | JsonParseException e) {
            System.out.println("Ошибка парсинга");
            return false;
        }
        catch (Exception e) {
            System.out.println("Ошибка парсинга");
            return false;
        }
    }

    /**
     * Вспомогательная функция для парсинга элемента коллекции введёного пользователем. Использует gson.
     * @param scanner сканер для считывания ввода.
     * @return возвращает спарсенный объект.
     */
    private static Creature startParsing(Scanner scanner, String iJson) {
        boolean flag = true;
        String json = ""+iJson;
        if (((json.length()- json.replaceAll("\\{","").length()) - (json.length()- json.replaceAll("\\}","").length()))<1) flag = false;

        while (flag) {
            json = json + scanner.nextLine();
            if (((json.length()- json.replaceAll("\\{","").length()) - (json.length()- json.replaceAll("\\}","").length()))<1) flag = false;
        }
        try {
            return gson.fromJson(json, Human.class);
        } catch (Exception e) {
            try {
                return gson.fromJson(json, Animal.class);
            } catch (Exception ex) {
                return gson.fromJson(json, Shoggot.class);
            }
        }
    }

    /**
     * Вспомогательная функция для парсинга. Анализирует полученную на вход строку на правильность.
     * @param line строка вида "String"
     * @return Возвращает сроку вида String, если формат строки правильный.
     */
    private static String findString(String line) {
        String subLine[] = line.split("\"");
        if (subLine.length == 3 || subLine.length == 2) return subLine[1];
        else return null;
    }

    /**
     * Вспомогательная функция для парсинга элемента прочитанного из файла
     * @param csv - строка следуюещего формата [key],[type],[location],[name]
     * @return Возвращает уже спарсенный элемент.
     */
    private static Creature pasrseCSV(String csv) {

        String param[] = csv.split(",");
        if (!(param.length == 7 || param.length == 8)) return null;
        int k = 6;
        if (param.length == 8) k = 7;
        param[k] = param[k].replace("\r", "");
        param[6] = findString(param[6]);
        param[2] = findString(param[2]);
        param[1] = param[1].toUpperCase();
        if (param[1].equals("HUMAN") || param[1].equals("ANIMAL") || param[1].equals("SHOGGOT"))
        switch(param[1]) {
                case "ANIMAL":
                    return new Animal(param[2], Animals.valueOf(param[k].toUpperCase()), new Locations(Double.valueOf(param[3]), Double.valueOf(param[4]),param[6], Boolean.valueOf(param[5])));
                case "HUMAN":
                    return new Human(param[2], new Locations(Double.valueOf(param[3]), Double.valueOf(param[4]),param[6], Boolean.valueOf(param[5])));
                case "SHOGGOT":
                    return new Shoggot(param[2], new Locations(Double.valueOf(param[3]), Double.valueOf(param[4]),param[6], Boolean.valueOf(param[5])));
            }
            return null;
    }

    /**
     * Вывод список команд при работе в интерактивном режиме
     */
    public static void help() {
        System.out.print("Команды при работе в интерактивном режиме:\n\n" +
                "insert {string} {element} - добавить новый элемент с заданным ключом, {element} должен быть в формате json,а String - строка без пробельных символов\n" +
                "Пример:\n" +
                "insert ExampleKey {\n" +
                "   \"type\": \"Animal\",\n" +
                "   \"kindOfAnimal\":\"DOG\",\n" +
                "   \"name\":\"Эффа\",\n" +
                "   \"loc\":\n" +
                "       {\n" +
                "       \"x\":10.0,\n" +
                "       \"y\":30.0,\n" +
                "       \"name\":\"Будка\",\n" +
                "       \"isBuilding\":true\n" +
                "       }\n" +
                "}\n" +
                "Поле kindOfAnimal может принимать следующие значения: DOG, PIG, DUCK, BEAR, CAT, WOLF, SHEEP, PENGUIN\n" +
                "Поле isBuilding может быть не указано, тогда будет считаться, что его значение - false\n"+
                "Вместо Animal могут быть два других типа: Human и Shoggot;\n\n" +
                "remove_greater {element} - удалить из коллекции все элементы длина поля Name которых, больше длины поля Name у {element}.\n\n" +
                "show - вывести список элементов коллекции.\n\n" +
                "help - помощь.\n\n" +
                "clear - удалить из коллекции все элементы.\n\n" +
                "import {string path} - добавить в коллекцию элементы из файла, где {string path) - путь к файлу.\n\n" +
                "info - вывести информацию о коллекции.\n\n" +
                "remove {string} - удалить элемент из коллекции по его ключу.\n\n" +
                "exit - выйти из программы и сохранить текущую коллекцию в файле, который был указан при запуске.\n");
    }
}
