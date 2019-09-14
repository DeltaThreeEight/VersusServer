package Server;

import Entities.Human;
import Entities.Moves;
import Exceptions.NotAliveException;
import Server.Commands.ClientCommand;
import World.Location;
import World.WorldManager;


class ClientCommandHandler {

    private WorldManager world;
    private Server server;

    ClientCommandHandler(Server server) {
        this.server = server;
        this.world = server.getWorld();
    }

    void executeCommand(Client client, ClientCommand command) {
        if (client.isAuthorized()) {
            switch (command.getCommandName()) {
                case "show":
                    client.showHumans();
                    break;
                case "show_all":
                    world.showHumansFor(client);
                    break;
                case "showstats":
                    showStats(client, command.getArg(0));
                    break;
                case "chat":
                    server.sendToAllClients(command.getArgsAsOne(), client);
                    break;
                case "deauth":
                    deauth(client);
                    break;
                case "select":
                    trySelect(client, command.getArg(0));
                    break;
                case "createnew":
                    createNewPerson(client, command.getPerson());
                    break;
                case "remove":
                    removePerson(client, command.getArg(0));
                    break;
                case "teleport":
                    teleportPerson(client, command.getArg(0), command.getArg(1));
                    break;
                case "hit":
                    hitPerson(command.getArg(0));
                    break;
                case "shoot":
                    shoot(client);
                    break;
                case "move":
                    move(client, command.getArg(0));
                    break;
                case "rotare":
                    rotare(client, command.getArg(0));
                    break;
                case "login":
                    client.sendMessage(Actions.ALERT, "ALREADY_AUTH");
                    break;
                case "register":
                    client.sendMessage(Actions.ALERT, "ALREADY_REG");
                    break;
                case "exit":
                    client.closeConnection();
                    break;
            }
        } else {
            switch (command.getCommandName()) {
                case "login":
                    tryLogin(client, command.getArg(0), command.getArg(1));
                    break;
                case "register":
                    register(client, command.getArg(0), command.getArg(1), command.getArg(2));
                    break;
                default:
                    client.sendMessage(Actions.ALERT, "NOT_AUTH");
            }
        }
    }

    private void register(Client client, String login, String password, String email) {
        if (server.getDBC().executeRegister(login, password, email))
            client.sendMessage(Actions.ALERT, "REG_SUCCESS");
        else
            client.sendMessage(Actions.ALERT, "NOT_UNIQUE");
    }

    private void tryLogin(Client client, String login, String pass) {
        switch (server.getDBC().executeLogin(login, pass)) {
            case 0:
                authorize(client, login);
                break;
            case 1:
                client.sendMessage(Actions.REQUESTTOKEN, "UNCONF_TOKEN");
                String token = client.readCMD().getArg(0);

                if (server.getDBC().checkRegToken(client, login, token))
                    authorize(client, login);

                break;
            case 2:
                client.sendMessage(Actions.ALERT, "WRONG_LOG_PASS");
                break;
        }
    }

    private void rotare(Client client, String direction) {
        server.rotarePLR(client, direction);
    }

    private void move(Client client, String direction) {
        if (client.getHuman() != null) {
            Moves move;
            try {
                move = Moves.valueOf(direction.toUpperCase());
                server.movPlayer(client, move);
            } catch (NotAliveException e) {
                client.sendMessage(Actions.ALERT, e.getMessage());
            } catch (Exception e) {
                System.err.println("Неправильно указано направление движения");
            }
        }
    }

    private void shoot(Client client) {
        Human person = client.getHuman();

        if (person.getAmmo() > 0) {
            server.shootFromClient(client);
        } else if (!client.isReloading()){
            new Thread(() -> {
                client.setReloading(true);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }

                person.reload();
                client.sendMessage(Actions.RELOAD, person.getName());

                client.setReloading(false);
            }).start();
        }

    }

    private void hitPerson(String arg) {
        Human hitted = world.getHuman(arg);

        synchronized (hitted) {
            hitted.hit();

            if (!hitted.isAlive()) {
                server.killPlayer(hitted.getName());

                server.getClients()
                        .stream()
                        .filter(c -> c.getUserName().equals(hitted.getUser()))
                        .forEach(c -> {
                            c.setHuman(null);
                            c.removeHuman(hitted.getName());
                        });

                server.getPuddles().add(new Location(hitted.getLocation().getX(), hitted.getLocation().getY()));
            }
        }
    }

    public void authorize(Client client, String username) {

        if (isUserOnServer(username)) {
            client.sendMessage(Actions.ALERT, "USER_ALREADY_AUTH");
            return;
        }

        client.setAuthorized(true);
        client.setUserName(username);

        client.sendMessage(Actions.ALERT, "AUTH_SUCCESS");
        server.sendToAllClients(client.getUserName()+ " AUTHORIZED", null);

        server.getDBC().sendPersons(client);

        server.loadPLRS(client);
        server.loadPDLS(client);
    }

    public void deauth(Client client) {

        if (client.getHuman() != null)
            server.hidePlayer(client.getHuman().getName());

        client.setAuthorized(false);
        server.sendToAllClients(client.getUserName() + " LEFT_SERVER", null);

    }

    private void showStats(Client client, String arg) {
        Human stat = world.getHuman(arg);
        if (stat != null) {
            client.sendMessage(Actions.STATS, null, stat);
        } else
            client.sendMessage(Actions.ALERT, "NO_PERSON");
    }

    private void trySelect(Client client, String arg) {

        Human sel = client.getPersons().get(arg);

        if (sel != null) {

            if (client.getHuman() == sel)
                client.sendMessage(Actions.ALERT, "PERSON_ALREADY_SELECTED");
            else
                client.setHuman(sel);

        } else
            client.sendMessage(Actions.ALERT, "NO_PERSON");
    }

    private void createNewPerson(Client client, Human person) {
        if (world.getHuman(person.getName()) == null) {

            person.setUser(client.getUserName());

            server.addNewPerson(client, person);
        } else {
            client.readHuman();
            client.sendMessage(Actions.ALERT, "SAME_NAME");
        }
    }

    private void removePerson(Client client, String arg) {

        Human person = client.getPersons().get(arg);

        if (person != null) {

            if (client.getHuman() == person)
                client.setHuman(null);

            client.removeHuman(person.getName());
            client.sendMessage(Actions.ALERT, "PERSON_REMOVED");

        } else {
            client.sendMessage(Actions.ALERT, "NO_PERSON");
        }
    }

    private void teleportPerson(Client client, String arg1, String arg2) {
        try {

            double x = Double.parseDouble(arg1);
            double y = Double.parseDouble(arg2);

            server.tpPlayer(client, x, y);
        } catch (NotAliveException e) {
            client.sendMessage(Actions.ALERT, e.getMessage());
        }
    }

    private boolean isUserOnServer(String user) {
        return server.getClients().stream()
                .anyMatch(c -> c.getUserName().equals(user) && c.isTokenValid());
    }

}
