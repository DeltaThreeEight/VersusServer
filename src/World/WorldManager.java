package World;

import Entities.Human;
import Server.Client;
import Server.Actions;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {

    private final Map<String, Human> persons = new ConcurrentHashMap<>();
    private final Date dateInit = new Date();

    public  boolean removeHuman(String name) {
        if (persons.remove(name) != null) {
            System.out.println("Персонаж успешно удалён");
            return true;
        }
        else {
            System.out.println("Такого персонажа нет");
            return false;
        }
    }

    public void addNewHuman(Human human) {
        persons.put(human.getName(), human);
    }

    public Human getHuman(String creature) {
        return persons.get(creature);
    }

    public void showHumans() {
        System.out.println("Список элементов коллекции: ");
        persons.keySet().stream().forEach(k -> System.out.printf("%s: %s\n", k, persons.get(k)));
    }

    public void showHumansFor(Client client) {
        String[] names = persons.values().stream().map(h -> h.getName() + " [" + h.getUser() + "]").toArray(String[]::new);
        client.sendMessage(Actions.SENDALLPERSONS, null, names);
    }

    public void getInfo() {
        System.out.printf("Дата инициализации: %s\n"
        + "Тип: %s\n"
        + "Количество элементов: %s\n", dateInit, persons.getClass(), persons.size());
    }

    public void clear() {
        persons.clear();
        System.out.println("Коллекция успешно очищена");
    }

    public Map<String, Human> getPersons() {
        return persons;
    }

}
