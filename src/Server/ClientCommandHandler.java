package Server;

import Entities.Human;
import Entities.Moves;
import Exceptions.NotAliveException;
import World.WorldManager;

import java.io.IOException;


class ClientCommandHandler {

    private Client client;
    private WorldManager wrldMngr = WorldManager.getInstance();
    private Server server;

    ClientCommandHandler(Client client, Server server) {
        this.client = client;
        this.server = server;
    }

    void executeCommand(String commnd) throws IOException {
        String command[] = commnd.split("\\$");
        String[] commands = command[0].split(" ");
        if (commands.length == 0) return;
        if (!commands[0].equals("register") && !commands[0].equals("login") && !commands[0].equals("exit")) {
            if (!server.getDBC().checkAuthToken(client, client.getUserName(), command[1])) return;
        }
        switch(commands[0]) {
            case "login":
                int login_code = server.getDBC().executeLogin(commands[1], commands[2]);
                client.setIsAuth(login_code == 0);
                if (client.getIsAuth()) {
                    if (server.getClients().stream().anyMatch(c -> c.getUserName().equals(commands[1]) && c.isTokenValid())) {
                        client.sendMessage(cActions.ALERT, "Данный пользователь уже авторизован!\n");
                        client.setIsAuth(false);
                        break;
                    }
                    client.sendMessage(cActions.ALERT, "Авторизация успешна\n");
                    client.setIsTokenValid(true);
                    client.setUserName(commands[1]);
                    setAuthToken(client);
                    server.sendToAllClients(client.getUserName()+ " авторизовался.", null);
                    server.getDBC().loadPersons(client);

                }
                else {
                    if (login_code == 2) client.sendMessage(cActions.ALERT, "Неверный логин/пароль\n");
                    if (login_code == 1) {
                        client.sendMessage(cActions.SENDTOKEN, "Почта не подтверждена. Введите токен, указанный в письме\n");
                        String token = client.readLine();
                        server.getDBC().checkRegToken(client, commands[1], token.replace("$null", ""));
                    }
                }
                break;
            case "register":
                if (commands.length > 2)
                if (server.getDBC().executeRegister(commands[1], commands[2], commands[3])) {
                    client.sendMessage(cActions.ALERT, "Регистрация успешна. На почту должно придти письмо с подтверждением регистрации. Теперь вы можете авторизоваться.\n");
                } else client.sendMessage(cActions.ALERT, "Пользователь с таким именем/почтой уже есть\n");
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
            case "showstats":
                Human stat = wrldMngr.getHuman(client.getUserName()+commands[1]);
                if (stat != null) sendMessage(cActions.ALERT, "Имя: "+stat.getName()
                +"\nЗдоровье: "+stat.getHealth()
                +"\nДата создания: "+stat.getDate());
                else sendMessage(cActions.ALERT, "У вас нет такого персонажа");
                break;
            case "chat":
                server.sendToAllClients(command[0].replace(commands[0]+" ", ""), client);
                break;
            case "select":
                if (commands.length < 2)
                    sendMessage(cActions.SEND,"Отсутсвуют аргументы\n");
                else {
                    boolean found;
                    found = client.getPersons().keySet().stream().anyMatch(v -> v.equals(commands[1]));
                    Human sel = wrldMngr.getHuman(client.getUserName()+commands[1]);
                    if (found) {
                        if (client.getHuman() != null && sel.getName().equals(client.getHuman().getName())) {
                            sendMessage(cActions.ALERT, "Вы уже выбрали этого персонажа!\n");
                            break;
                        }
                        if (client.getKey() != null) server.remPlayer(client.getKey());
                        client.setKey(commands[1]);
                        client.setHuman(sel);
                        sendMessage(cActions.ALERT, "Выбран персонаж: " + sel.getName() + "\n");
                        sendMessage(cActions.DESERIALIZE, sel);
                        server.addPlayer(client, commands[1], sel);
                    } else sendMessage(cActions.ALERT, "У вас нет такого персонажа\n");
                }
                break;
            case "createnew":
                if (client.getPersons().get(command[0].replaceFirst(commands[0]+" ", "")) == null) {
                    if (client.getKey() != null) server.remPlayer(client.getKey());
                    Human human = (Human) client.readObject();
                    wrldMngr.addNewHuman(client.getUserName()+commands[1], human, client.getUserName());
                    server.getDBC().addToDB(client.getUserName(), human);
                    client.addHuman(commands[1], human);
                    server.addPlayer(client, commands[1], human);
                    client.showHumans();
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
                    server.getDBC().removePerson(client.getUserName(), person.getName());
                    sendMessage(cActions.ALERT, "Персонаж успешно удалён\n");
                    client.showHumans();
                } else {
                    sendMessage(cActions.ALERT, "У вас нет персонажа с таким именем\n");
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
                            System.out.println("Неправильно указано направление движения");
                        }
                        if (move != null) {
                            try {
                                Human crte = client.getHuman();
                                crte.move(move);
                                server.movPlayer(client, client.getKey(), move);
                            } catch (NotAliveException e) {
                                sendMessage(cActions.ALERT, e.getMessage());
                            }
                        }
                    }
                }
                break;
            case "exit":
                if (client.getKey() != null)
                    server.remPlayer(client.getKey());
                if (client.getIsAuth()) server.sendToAllClients(client.getUserName()+ " отключился от сервера.", null);
                break;
        }
    }

    private void sendMessage(cActions action, String str) {
        client.sendMessage(action, str);
    }

    private void sendMessage(cActions action, Object obj) {
        client.sendMessage(action, "", obj);
    }

    private void helpClient() {
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

    void setAuthToken(Client client) {
        String token = DataBaseConnection.getToken();
        client.setIsAuth(true);
        client.sendMessage(cActions.AUTH, token);
        server.getDBC().setAuthToken(client.getUserName(), token);
    }
}
