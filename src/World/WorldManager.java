package World;

import Entities.Human;
import Server.*;

import java.io.DataOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {


    private final Map<String, Human> humans = new ConcurrentHashMap<>();
    private final Date dateInit = new Date();
    private static WorldManager wrld = null;

    private WorldManager() {}

    /**
     * Удаляет элемент коллекции по его ключу.
     * @param username - Название(ключ) элемента коллекции.
     * @return Возвращает true, если элемент удалён, false - если нет.
     */
    public  boolean removeHuman(String username , String name) {
        if (humans.remove(username+name) != null) {
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
    public void addNewHuman(String key, Human iCreature, String username) {
        iCreature.setUser(username);
        humans.put(key, iCreature);
    }

    /**
     * Запрос элемента коллекции по его ключу.
     * @param creature - Ключ элемента коллекции
     * @return Возвращает какого-либо наследника Creature, если такой был найден по ключу.
     */
    public Human getHuman(String creature) {
        return humans.get(creature);
    }

    /**
     * Вывести в стандатный поток вывода все ключи коллекции.
     */
    public void showHumans() {
        System.out.println("Список элементов коллекции: ");
        humans.keySet().stream().forEach(k -> System.out.println(k + " " + humans.get(k)));
    }

    public void showHumansFor(Client client) {
        client.sendMessage(cActions.SEND, "Список персонажей всех игроков:\n");
        humans.keySet().stream()
                .map(k -> humans.get(k).getName()+" ["+humans.get(k).getUser()+"]\n")
                .forEach(p -> client.sendMessage(cActions.SEND, p));
    }
    /**
     * Вывести в стандартый поток вывода информацию о коллекции...
     */
    public void getInfo() {
        System.out.println("Дата инициализации: " +dateInit+"\n"
        + "Тип: HashMap\n"
        + "Количество элементов: " + humans.size());
    }

    /**
     * Удалить из коллекции все объекты(вообще все).
     */
    public void clear() {
        humans.clear();
        System.out.println("Коллекция успешно очищена");
    }

    public static WorldManager getInstance() {
        if (wrld == null) {
            wrld = new WorldManager();
            return wrld;
        } else return wrld;
    }

    /**
     * Удаляет все элементы из коллекции длина имени которых больше, чем длина имени элемента указанного в качестве параметра.
     * @param human Любой наследник Creature.
     */
    public void removeGreater(Human human) {
        humans.keySet()
                .stream()
                .filter(e -> human.getName().length() < humans.get(e).getName().length())
                .forEach(humans::remove);
        System.out.println("Элементы превышающие \""+human.getName()+"\" удалены из коллекции");
    }

    /**
     * Возвращает коллекцию в текущем состоянии.
     */
    public Map<String, Human> getHumans() {
        return humans;
    }

}
