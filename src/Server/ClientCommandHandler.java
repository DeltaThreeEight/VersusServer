package Server;

import Entities.Human;
import Entities.Moves;
import Exceptions.NotAliveException;
import World.WorldManager;


class ClientCommandHandler {

    private Client client;
    private WorldManager wrldMngr = WorldManager.getInstance();
    private Server server;

    ClientCommandHandler(Client client, Server server) {
        this.client = client;
        this.server = server;
    }

    void executeCommand(Command cmd) {

        if (!isAuthCMD(cmd))
            if (!server.getDBC().checkAuthToken(client, client.getUserName(), cmd.getToken())) return;

        switch(cmd.getName()) {
            case "login":
                int login_code = server.getDBC().executeLogin(cmd.getArgs()[0], cmd.getArgs()[1]);

                switch (login_code) {
                    case 0:
                        auth(cmd);
                        break;
                    case 1:
                        client.sendMessage(cActions.SENDTOKEN, "UNCONF_TOKEN");
                        String token = client.readCMD().getArgs()[0];
                        server.getDBC().checkRegToken(client, cmd.getArgs()[0], token);
                        break;
                    case 2:
                        client.sendMessage(cActions.ALERT, "WRONG_LOG_PASS");
                        break;
                }

                break;
            case "register":
                if (server.getDBC().executeRegister(cmd.getArgs()[0], cmd.getArgs()[1], cmd.getArgs()[2])) {
                    client.sendMessage(cActions.ALERT, "REG_SUCCESS");
                } else client.sendMessage(cActions.ALERT, "NOT_UNIQUE");
                break;
            case "show":
                client.showHumans();
                break;
            case "show_all":
                WorldManager.getInstance().showHumansFor(client);
                break;
            case "showstats":
                Human stat = wrldMngr.getHuman(cmd.getArgs()[0]);
                if (stat != null) {
                    sendMessage(cActions.STATS, stat);
                }
                else sendMessage(cActions.ALERT, "NO_PERSON");
                break;
            case "chat":
                server.sendToAllClients(cmd.getArgs()[0], client);
                break;
            case "deauth":
                deauth();
                break;
            case "select":
                    String prsnName = cmd.getArgs()[0];
                    Human sel = client.getPersons().get(prsnName);
                    if (sel != null) {
                        if (client.getHuman() != null && sel.getName().equals(client.getHuman().getName())) {
                            sendMessage(cActions.ALERT, "PERSON_ALREADY_SELECTED");
                            break;
                        }
                        if (client.getHuman() != null)
                            server.remPlayer(client.getHuman().getName());
                        client.setKey(sel.getName());
                        client.setHuman(sel);
                        sendMessage(cActions.ALERT, "PERSON_SELECTED " + sel.getName());
                        sendMessage(cActions.DESERIALIZE, sel);
                        server.addPlayer(client, sel);
                    } else sendMessage(cActions.ALERT, "NO_PERSON");
                break;
            case "createnew":
                String name = cmd.getArgs()[0];
                if (wrldMngr.getHuman(name) == null) {
                    Human human = client.readHuman();
                    wrldMngr.addNewHuman(name, human, client.getUserName());
                    server.getDBC().addToDB(client.getUserName(), human);
                    client.addHuman(human);
                    client.showHumans();
                } else {
                    client.readHuman();
                    sendMessage(cActions.ALERT, "SAME_NAME");
                }
                break;
            case "remove":
                String name_rem = cmd.getArgs()[0];
                Human person = client.getPersons().get(name_rem);
                if (person != null) {
                    if (client.getHuman() != null && client.getHuman().getName().equals(person.getName())) {
                        server.remPlayer(client.getHuman().getName());
                        client.setHuman(null);
                        client.setKey(null);
                    }
                    wrldMngr.removeHuman(person.getName());
                    client.removeHuman(person.getName());
                    server.getDBC().removePerson(client.getUserName(), person.getName());
                    sendMessage(cActions.ALERT, "PERSON_REMOVED");
                    client.showHumans();
                } else {
                    sendMessage(cActions.ALERT, "NO_PERSON");
                }
                break;
            case "teleport":
                try {
                    Human crte = client.getHuman();
                    double x = Double.parseDouble(cmd.getArgs()[0]);
                    double y = Double.parseDouble(cmd.getArgs()[1]);
                    crte.teleportOther(x, y);
                    server.tpPlayer(client, x, y);
                } catch (NotAliveException e) {
                    sendMessage(cActions.ALERT, e.getMessage());
                }
                break;
            case "move":
                    if (client.getHuman() != null) {
                        Moves move = null;
                        try {
                            move = Moves.valueOf(cmd.getArgs()[0].toUpperCase());
                        } catch (Exception e) {
                            System.err.println("Неправильно указано направление движения");
                        }
                        if (move != null) {
                            try {
                                Human crte = client.getHuman();
                                crte.move(move);
                                server.movPlayer(client, move);
                            } catch (NotAliveException e) {
                                sendMessage(cActions.ALERT, e.getMessage());
                            }
                        }
                    }
                break;
            case "exit":
                client.closeConnection();
                break;
        }
    }

    public boolean auth(Command cmd) {
        if (isUsrOnServer(cmd.getArgs()[0])) {
            client.sendMessage(cActions.ALERT, "USER_ALREADY_AUTH");
            return false;
        }
        client.setIsAuth(true);
        client.setIsTokenValid(true);
        client.setUserName(cmd.getArgs()[0]);
        setAuthToken(client);
        server.getDBC().loadPersons(client);
        client.sendMessage(cActions.ALERT, "AUTH_SUCCESS");
        server.loadPLRS(client);
        server.sendToAllClients(client.getUserName()+ " AUTHORIZED", null);
        return true;
    }

    public void deauth() {
        if (client.getHuman() != null) {
            server.remPlayer(client.getKey());
        }
        client.setHuman(null);
        client.setIsAuth(false);
        client.setKey(null);
        server.sendToAllClients(client.getUserName() + " LEFT_SERVER", null);
        client.setUserName(DataBaseConnection.getToken());
        client.getPersons().clear();
        client.sendMessage(cActions.DEAUTH, null);
    }

    private void sendMessage(cActions action, String str) {
        client.sendMessage(action, str);
    }

    private boolean isUsrOnServer(String user) {
        return server.getClients().stream()
                .anyMatch(c -> c.getUserName().equals(user) && c.isTokenValid());
    }

    private boolean isAuthCMD(Command cmd) {
        if (cmd.getName().equals("register") || cmd.getName().equals("login") || cmd.getName().equals("deauth"))
            return true;
        else return false;
    }

    private void sendMessage(cActions action, Object obj) {
        client.sendMessage(action, "", obj);
    }

    void setAuthToken(Client client) {
        String token = DataBaseConnection.getToken();
        client.setIsAuth(true);
        client.sendMessage(cActions.AUTH, token);
        server.getDBC().setAuthToken(client.getUserName(), token);
    }
}
