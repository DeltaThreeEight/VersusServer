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
                        client.sendMessage(cActions.ALERT, "USER_ALREADY_AUTH");
                        client.setIsAuth(false);
                        break;
                    }
                    client.sendMessage(cActions.ALERT, "AUTH_SUCCESS");
                    client.setIsTokenValid(true);
                    client.setUserName(commands[1]);
                    setAuthToken(client);
                    server.sendToAllClients(client.getUserName()+ " AUTHORIZED", null);
                    server.getDBC().loadPersons(client);
                    server.loadPLRS(client);
                }
                else {
                    if (login_code == 2) client.sendMessage(cActions.ALERT, "WRONG_LOG_PASS");
                    if (login_code == 1) {
                        client.sendMessage(cActions.SENDTOKEN, "UNCONF_TOKEN");
                        String token = client.readLine();
                        server.getDBC().checkRegToken(client, commands[1], token.replace("$null", ""));
                    }
                }
                break;
            case "register":
                if (commands.length > 2)
                if (server.getDBC().executeRegister(commands[1], commands[2], commands[3])) {
                    client.sendMessage(cActions.ALERT, "REG_SUCCESS");
                } else client.sendMessage(cActions.ALERT, "NOT_UNIQUE");
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
                if (stat != null) {
                    sendMessage(cActions.STATS, stat);
                }
                else sendMessage(cActions.ALERT, "NO_PERSON");
                break;
            case "chat":
                server.sendToAllClients(command[0].replace(commands[0]+" ", ""), client);
                break;
            case "deauth":
                if (client.getHuman() != null) {
                    server.remPlayer(client.getKey());
                }
                server.sendToAllClients(client.getUserName() + " LEFT_SERVER", null);
                client.setHuman(null);
                client.setIsAuth(false);
                client.setKey(null);
                client.setUserName("0");
                client.getPersons().clear();
                break;
            case "select":
                if (commands.length < 2)
                    sendMessage(cActions.SEND,"Отсутсвуют аргументы\n");
                else {
                    boolean found;
                    found = client.getPersons().keySet().stream().anyMatch(v -> v.equals(commands[1]));
                    Human sel = wrldMngr.getHuman(client.getUserName()+commands[1]);
                    if (found && sel != null) {
                        if (client.getHuman() != null && sel.getName().equals(client.getHuman().getName())) {
                            sendMessage(cActions.ALERT, "PERSON_ALREADY_SELECTED");
                            break;
                        }
                        if (client.getKey() != null) server.remPlayer(client.getKey());
                        client.setKey(commands[1]);
                        client.setHuman(sel);
                        sendMessage(cActions.ALERT, "PERSON_SELECTED " + sel.getName());
                        sendMessage(cActions.DESERIALIZE, sel);
                        server.addPlayer(client, commands[1], sel);
                    } else sendMessage(cActions.ALERT, "NO_PERSON");
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
                    sendMessage(cActions.ALERT, "SAME_NAME");
                }
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
                    sendMessage(cActions.ALERT, "PERSON_REMOVED");
                    client.showHumans();
                } else {
                    sendMessage(cActions.ALERT, "NO_PERSON");
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
                if (client.getIsAuth()) server.sendToAllClients(client.getUserName() + " LEFT_SERVER", null);
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
