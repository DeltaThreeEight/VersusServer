package Server;

import Entities.Human;
import Entities.Moves;
import Exceptions.NotAliveException;
import World.WorldManager;

import java.io.IOException;


public class ClientCommandHandler {

    private Client client;
    private WorldManager wrldMngr = WorldManager.getInstance();
    private Server server;

    public ClientCommandHandler(Client client, Server server) {
        this.client = client;
        this.server = server;
    }

    public void executeCommand(String commnd) throws IOException {
        String command[] = commnd.split("\\$");
        String[] commands = command[0].split(" ");
        if (commands.length == 0) return;
        if (!commands[0].equals("register") && !commands[0].equals("login") && !commands[0].equals("exit")) {
            if (!server.getDBC().checkAuthToken(client, client.getUserName(), command[1])) return;
        }
        switch(commands[0]) {
            case "login":
                int login_code = server.getDBC().executeLogin(commands[1], commands[2]);
                if (commands.length > 2) client.setIsAuth(login_code == 0);
                else client.sendMessage(cActions.SEND, "Авторизация не удалась\n");
                if (client.getIsAuth()) {
                    if (server.getClients().stream().anyMatch(c -> c.getUserName().equals(commands[1]))) {
                        client.sendMessage(cActions.SEND, "Данный пользователь уже авторизован!\n");
                        client.setIsAuth(false);
                        break;
                    }
                    client.sendMessage(cActions.SEND, "Авторизация успешна\n");
                    client.setUserName(commands[1]);
                    setAuthToken(client);
                    server.sendToAllClients(client.getUserName()+ " авторизовался.", null);
                    server.getDBC().loadPersons(client);

                }
                else {
                    if (login_code == 2) client.sendMessage(cActions.SEND, "Неверный логин/пароль\n");
                    if (login_code == 1) {
                        client.sendMessage(cActions.SEND, "Почта не подтверждена. Введите токен, указанный в письме\n");
                        String token = client.readLine();
                        server.getDBC().checkRegToken(client, commands[1], token.replace("$null", ""));
                    }
                }
                break;
            case "register":
                if (commands.length > 2)
                if (server.getDBC().executeRegister(commands[1], commands[2], commands[3])) {
                    client.sendMessage(cActions.SEND, "Регистрация успешна. На почту должно придти письмо с подтверждением регистрации\n");
                } else client.sendMessage(cActions.SEND, "Пользователь с таким именем/почтой уже есть\n");
                break;
            case "show":
                client.showHumans();
                break;
            case "show_all":
                WorldManager.getInstance().showHumansFor(client);
                break;
            case "help":
                helpClient();
                break;
            case "chat":
                server.sendToAllClients(command[0].replace(commands[0]+" ", ""), client);
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
                if (client.getPersons().get(command[0].replaceFirst(commands[0]+" ", "")) == null) {
                    if (client.getKey() != null) server.remPlayer(client.getKey());
                    Human human = (Human) client.readObject();
                    wrldMngr.addNewHuman(client.getUserName()+commands[1], human, client.getUserName());
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
                Human person = client.getPersons().get(command[0].replaceFirst(commands[0]+" ", ""));
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
                "remove [имя персонажа] - удалить имеющегося персонажа\n" +
                "show - список ваших персонажей\n" +
                "show_all - список всех персонажей\n" +
                "chat [сообщение] - отправить сообщение другим игрокам\n" +
                "help - справка по командам\n" +
                "exit - отключиться от сервера\n");
    }

    public void setAuthToken(Client client) {
        String token = DataBaseConnection.getToken();
        client.setIsAuth(true);
        client.sendMessage(cActions.AUTH, token);
        server.getDBC().setAuthToken(client.getUserName(), token);
    }
}
