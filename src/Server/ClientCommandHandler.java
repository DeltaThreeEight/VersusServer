package Server;

import Entities.Human;
import Entities.Moves;
import Exceptions.NotAliveException;
import World.WorldManager;

public class ClientCommandHandler {

    private Client client;
    private WorldManager wrldMngr = WorldManager.getInstance();
    private Server server = null;

    public ClientCommandHandler(Client client, Server server) {
        this.client = client;
        this.server = server;
    }

    public void executeCommand(String command) {
        String[] commands = command.split(" ");
        if (commands.length == 0) return;

        switch(commands[0]) {
            case "login":
                if (commands.length > 2) client.setIsAuth(server.getDBC().executeLogin(commands[1], commands[2]));
                else client.sendMessage(cActions.SEND, "Авторизация не удалась\n");
                if (client.getIsAuth()) {
                    if (server.getClients().stream().anyMatch(c -> c.getUserName().equals(commands[1]))) {
                        client.sendMessage(cActions.SEND, "Данный пользователь уже авторизован!\n");
                        client.setIsAuth(false);
                        break;
                    }
                    client.sendMessage(cActions.SEND, "Авторизация успешна\n");
                    client.sendMessage(cActions.AUTH, null);
                    client.setUserName(commands[1]);
                    server.sendToAllClients(client.getUserName()+ " авторизовался.", null);
                    server.getDBC().loadPersons(client);
                }
                else client.sendMessage(cActions.SEND, "Неверный логин/пароль\n");
                break;
            case "register":
                if (commands.length > 3) client.setIsAuth(server.getDBC().executeRegister(commands[1], commands[2], commands[3]));
                else client.sendMessage(cActions.SEND, "Регистрация не удалась\n");
                if (client.getIsAuth()) {
                    client.sendMessage(cActions.SEND, "Регистрация успешна\n");
                    client.sendMessage(cActions.AUTH, null);
                    client.setUserName(commands[1]);
                    server.getDBC().loadPersons(client);
                    server.sendToAllClients(client.getUserName()+ " авторизовался", null);
                } else client.sendMessage(cActions.SEND, "Пользователь с таким именем/почтой уже есть\n");
                break;
            case "show":
                client.showHumans();
                break;
            case "help":
                helpClient();
                break;
            case "chat":
                server.sendToAllClients(command.replace(commands[0]+" ", ""), client);
                break;
            case "select":
                if (commands.length < 2)
                    sendMessage(cActions.SEND,"Отсутсвуют аргументы\n");
                else {
                    Boolean found;
                    found = client.getPersons().keySet().stream().anyMatch(v -> v.equals(commands[1]));
                    Human sel = wrldMngr.getHuman(client.getUserName()+commands[1]);
                    if (client.getHuman() != null && sel.getName().equals(client.getHuman().getName())) {
                        sendMessage(cActions.SEND, "Вы уже выбрали этого персонажа!\n");
                        break;
                    }
                    if (found) {
                        if (client.getKey() != null) server.remPlayer(client.getKey());
                        client.setKey(commands[1]);
                        client.setHuman(sel);
                        sendMessage(cActions.SEND, "Выбран персонаж: " + sel.getName() + "\n");
                        sendMessage(cActions.DESERIALIZE, "", sel);
                        server.addPlayer(client, commands[1], sel);
                    } else sendMessage(cActions.SEND, "Персонаж не найден\n");
                }
                break;
            case "createnew":
                if (client.getPersons().get(command.replaceFirst(commands[0]+" ", "")) == null) {
                    if (client.getKey() != null) server.remPlayer(client.getKey());
                    Human human = (Human) client.readObject();
                    wrldMngr.addNewHuman(client.getUserName()+commands[1], human);
                    server.getDBC().addToDB(client.getUserName(), human);
                    client.addHuman(commands[1], human);
                    client.setHuman(human);
                    client.setKey(commands[1]);
                    server.addPlayer(client, commands[1], human);
                    sendMessage(cActions.SEND, "Выбран персонаж: " + human.getName() + "\n");
                    client.sendMessage(cActions.DESERIALIZE, "", human);
                } else {
                    client.readObject();
                    sendMessage(cActions.SEND, "У вас уже есть персонаж с этим именем\n");}
                break;
            case "remove":
                Human person = client.getPersons().get(command.replaceFirst(commands[0]+" ", ""));
                if (person != null) {
                    if (client.getKey() != null && client.getKey().equals(person.getName())) {
                        server.remPlayer(client.getKey());
                        client.setHuman(null);
                        client.setKey(null);
                    }
                    wrldMngr.removeHuman(client.getUserName(), person.getName());
                    client.removeHuman(person.getName());
                    sendMessage(cActions.SEND, "Персонаж успешно удалён\n");
                } else {
                    sendMessage(cActions.SEND, "У вас нет персонажа с таким именем\n");
                }
                break;
            case "move":
                if (commands.length < 2)
                    sendMessage(cActions.SEND,"Отсутсвуют аргументы\n");
                else {
                    if (client.getHuman() != null) {
                        Moves move = null;
                        try {
                            move = Moves.valueOf(commands[1].toUpperCase());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (move != null) {
                            try {
                                Human crte = client.getHuman();
                                crte.move(move);
                                sendMessage(cActions.SEND,"Перемещение успешно. Новые координаты: "+crte.getLocation().getName()+"\n");
                                server.movPlayer(client, client.getKey(), move);
                            } catch (NotAliveException e) {
                                sendMessage(cActions.SEND, e.getMessage());
                            }
                        }
                    }
                }
                break;
            case "exit":
                if (client.getKey() != null)
                    server.remPlayer(client.getKey());
                server.sendToAllClients(client.getUserName()+ " отключился от сервера.", null);
                break;
            default:
                sendMessage(cActions.SEND, "Команда не найдена\n");
        }
    }

    public void sendMessage(cActions action, String str) {
        client.sendMessage(action, str);
    }

    public void sendMessage(cActions action, String str, Object obj) {
        client.sendMessage(action, str, obj);
    }

    public void helpClient() {
        sendMessage(cActions.SEND, "Справка по командам: \n" +
                "select [идентифкатор персонажа] - выбрать персонажа, которого вы создали ранее\n" +
                "createnew - создать нового персонажа\n" +
                "move [направление] - переместить выбранного персонажа, где направление:left, right, forward, back\n" +
                "showstats - вывести информацию о персонаже\n" +
                "chat [сообщение] - отправить сообщение другим игрокам\n" +
                "help - справка по командам\n" +
                "exit - отключиться от сервера\n");
    }
}
