package World;

import Entities.Creature;
import java.util.*;

public class WorldManager {

    private static Weather weather = Weather.SUNNY;

    private static HashMap<String, Creature> creatures = new HashMap<String, Creature>();
    private static Date dateInit = new Date();

    private WorldManager(String iName) {
    }

    /**
     * Изменяет погоду в мире. Если дождь, то все существа, который вне здания промокнут.
     * @param wether - Погода
     */
    public static void changeWeather(Weather wether) {
        weather = wether;
        System.out.println("Изменение погоды на: "+weather);
        for (Creature c : creatures.values()) {
            if ((weather == Weather.RAIN) && (c.getLocation().getBuilding() == false))
                c.setWet(true);
            else if ((weather == Weather.SUNNY) && (c.getLocation().getBuilding() == false))
                c.setWet(false);
        }
    }

    /**
     * Возвращает погоду мира.
     * @return Погода.
     */
    public static Weather getWeather() {
        return weather;
    }

    /**
     * Удаляет элемент коллекции по его ключу.
     * @param key - Название(ключ) элемента коллекции.
     * @return Возвращает true, если элемент удалён, false - если нет.
     */
    public static boolean removeCreature(String key) {
        if (creatures.remove(key) != null) {
            System.out.println("Элемент успешно удалён");
            return true;
        }
        else {
            System.out.println("Не удалось удалить элемент");
            return false;
        }
    }

    /**
     * Добавляет в коллекцию новый элемент.
     * @param key - Название(ключ) элемента коллекции.
     * @param iCreature - Любой наследник Creature.
     */
    public static void addNewCreature(String key, Creature iCreature) {
        creatures.put(key, iCreature);
    }

    /**
     * Запрос элемента коллекции по его ключу.
     * @param creature - Ключ элемента коллекции
     * @return Возвращает какого-либо наследника Creature, если такой был найден по ключу.
     */
    public static Creature getCreature(String creature) {
        return creatures.get(creature);
    }

    /**
     * Вывести в стандатный поток вывода все ключи коллекции.
     */
    public static void showCreatures() {
        String showCreatures = "";
        showCreatures = showCreatures + "Список элементов коллекции:\n";
        for (String c : creatures.keySet()) {
            showCreatures = showCreatures + c.toString() + "\n";
        }
        System.out.println(showCreatures);
    }

    /**
     * Вывести в стандартый поток вывода информацию о коллекции...
     */
    public static void getInfo() {
        System.out.println("Дата инициализации: " +dateInit+"\n"
        + "Тип: HashMap\n"
        + "Количество элементов: " +creatures.size());
    }

    /**
     * Удалить из коллекции все объекты(вообще все).
     */
    public static void clear() {
        creatures.clear();
        System.out.println("Коллекция успешно очищена");
    }

    /**
     * Удаляет все элементы из коллекции длина имени которых больше, чем длина имени элемента указанного в качестве параметра.
     * @param creature Любой наследник Creature.
     */
    public static void removeGreater(Creature creature) {
        creatures.entrySet().removeIf(e -> creature.getName().length() < creatures.get(e.getKey()).getName().length());
        System.out.println("Элементы превышающие \""+creature.getName()+"\" удалены из коллекции");
    }

    /**
     * Возвращает коллекцию в текущем состоянии.
     */
    public static HashMap<String, Creature> getCreatures() {
        return creatures;
    }

}
