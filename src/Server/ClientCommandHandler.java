package Server;

import Entities.Creature;
import Entities.Human;
import Entities.Moves;
import Exceptions.MoveException;
import World.Locations;
import World.WorldManager;
import java.io.IOException;

public class ClientCommandHandler {

    private Client client;

    public ClientCommandHandler(Client client) {
        this.client = client;
    }

    public void executeCommand(String command) {
        String[] commands = command.split(" ");
        if (commands.length == 0) commands = new String[]{""};
        switch(commands[0]) {
            case "help":
                helpClient();
                break;
            case "select":
                if (commands.length < 2)
                    sendMessage("Отсутсвуют аргументы");
                else {
                    Creature sel = WorldManager.getCreature(commands[1]);
                    if (sel != null) {
                        client.setCreature(sel);
                        sendMessage("Выбран персонаж: " + sel.getName());
                        client.setName(sel.getName());
                    } else
                        sendMessage("Персонаж не найден");
                }
                break;
            case "showname":
                sendMessage(client.getName());
                break;
            case "createnew":
                sendMessage("Все пробелы в идентификатре будут удалены!\n" +
                        "Введите идентификатор персонажа, по котому его потом можно будет выбрать: ");
                String key = "";
                key = readLine().trim();
                String name = "";
                while (key.equals("")) {
                    sendMessage("Поле не должно быть пустым");
                    key = readLine().trim();
                }
                sendMessage("Введите имя персонажа: ");
                name = readLine();
                while (name.trim().equals("")) {
                    sendMessage("Поле не должно быть пустым");
                    name = readLine();
                }
                Creature crt = new Human(name, new Locations(0, 0));
                WorldManager.addNewCreature(key, crt);
                client.setCreature(crt);
                sendMessage("Персонаж успешно создан");
                client.setName(crt.getName());
                break;
            case "move":
                if (commands.length < 2)
                    sendMessage("Отсутсвуют аргументы");
                else {
                    if (client.getCreature() != null) {
                        Moves move = null;
                        try {
                            move = Moves.valueOf(commands[1].toUpperCase());
                        } catch (Exception e) {
                        }
                        if (move != null) {
                            try {
                                Creature crte = client.getCreature();
                                crte.move(move);
                                sendMessage("Перемещение успешно. Новые координаты: "+crte.getLocation().getName());
                            } catch (MoveException e) {
                                sendMessage(e.getMessage());
                            }
                        } else
                            sendMessage("Неверно указано направление движения");
                    } else
                        sendMessage("Не выбран персонаж");
                }
                break;
            case "exit":
                break;
            default:
                sendMessage("Команда не найдена");
        }
    }

    public String readLine() {
        try {
            return client.readLine();
        } catch (IOException e) {
            System.out.println("Соединение с клиентом потеряно.");
            return null;
        }
    }

    public void sendMessage(String str) {
        try {
            client.writeUTF(str);
        } catch (IOException e) {
            System.out.println("Невозможно отравить ответ клиенту.");
        }
    }

    public void helpClient() {
        sendMessage("Справка по командам: \n" +
                "select {string} - выбрать персонажа, которого вы создали ранее\n" +
                "createnew - создать нового персонажа\n" +
                "move {direction} - переместить выбранного персонажа, где {direction}:left, right, forward, back\n" +
                "showname - вывод ваше текущее имя на экран" +
                "help - справка по командам\n" +
                "exit - отключиться от сервера\n");
    }
}
