package World;

import Entities.Human;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {


    private static volatile Map<String, Human> humans = new ConcurrentHashMap<>();
    private static Date dateInit = new Date();

    private WorldManager() {}

    /**
     * Удаляет элемент коллекции по его ключу.
     * @param key - Название(ключ) элемента коллекции.
     * @return Возвращает true, если элемент удалён, false - если нет.
     */
    public static boolean removeHuman(String key) {
        if (humans.remove(key) != null) {
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
    public static void addNewHuman(String key, Human iCreature) {
        humans.put(key, iCreature);
    }

    /**
     * Запрос элемента коллекции по его ключу.
     * @param creature - Ключ элемента коллекции
     * @return Возвращает какого-либо наследника Creature, если такой был найден по ключу.
     */
    public static Human getHuman(String creature) {
        return humans.get(creature);
    }

    /**
     * Вывести в стандатный поток вывода все ключи коллекции.
     */
    public static void showHumans() {
        System.out.println("Список элементов коллекции: ");
        humans.keySet().stream().forEach(System.out::println);
    }

    /**
     * Вывести в стандартый поток вывода информацию о коллекции...
     */
    public static void getInfo() {
        System.out.println("Дата инициализации: " +dateInit+"\n"
        + "Тип: HashMap\n"
        + "Количество элементов: " + humans.size());
    }

    /**
     * Удалить из коллекции все объекты(вообще все).
     */
    public static void clear() {
        humans.clear();
        System.out.println("Коллекция успешно очищена");
    }

    /**
     * Удаляет все элементы из коллекции длина имени которых больше, чем длина имени элемента указанного в качестве параметра.
     * @param human Любой наследник Creature.
     */
    public static void removeGreater(Human human) {
        humans.keySet()
                .stream()
                .filter(e -> human.getName().length() < humans.get(e).getName().length())
                .forEach(humans::remove);
        System.out.println("Элементы превышающие \""+human.getName()+"\" удалены из коллекции");
    }

    /**
     * Возвращает коллекцию в текущем состоянии.
     */
    public static Map<String, Human> getHumans() {
        return humans;
    }

}
