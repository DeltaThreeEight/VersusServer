package Server;

import Entities.Human;
import Entities.Moves;
import Server.Commands.ClientCommand;
import World.Location;
import World.WorldManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable {
    private ServerSocket serverSocket = null;
    private List<Client> clients = new CopyOnWriteArrayList<Client>();
    private int port;
    private String host;
    private DataBaseConnection dataBaseConnection = null;
    private boolean isClosing = false;
    private List<Location> puddles = new CopyOnWriteArrayList<>();
    private WorldManager world;
    private ThreadGroup clientThreads;
    private ClientCommandHandler commandHandler;

    public List<Location> getPuddles() {
        return puddles;
    }

    public Server(String host, int port, WorldManager world) {
        this.port = port;
        this.host = host;
        this.world = world;
        commandHandler = new ClientCommandHandler(this);
    }

    public void run() {
        System.out.printf("Попытка запустить сервер на %s:%s\n", host, port);

        try {
            serverSocket = new ServerSocket(port, 100, InetAddress.getByName(host));

            System.out.printf("Сервер запущен! Адрес: %s\n", serverSocket.getInetAddress());
            System.out.printf("Порт: %s\n", serverSocket.getLocalPort());

            System.out.println("\nИнициализация соединения с БД...");

            dataBaseConnection = new DataBaseConnection(world, this);

            System.out.println("Загрузка персонажей...");
            int n = dataBaseConnection.loadPersonsFromDB();
            
            if (n == -1) {
                System.out.println("Попытка создать БД...");
                dataBaseConnection.createDB();
                System.out.println("БД успешно создана!");
                n = 0;
            }

            System.out.printf("Загружено %d персонажей.\n", n);

            // Перед выключением надо все данные загрузить в бд
            Runtime.getRuntime().addShutdownHook(new Thread(() ->
                dataBaseConnection.updatePersons()
            ));

        } catch (Exception e) {
            System.err.printf("Причина: %s\n", e.getMessage());
            System.err.println("Не очень хорошие проблемы... Прекращаю выполнение!");
            System.exit(-1);
        }

        clientThreads = new ThreadGroup("Clients");

        while (!isClosing) {
            Client client = new Client(waitConnection(), this);
            clients.add(client);

            Thread clientThread = new Thread(clientThreads, client);
            clientThread.start();
        }
    }


    void executeClientCommand(Client client, ClientCommand command) {
        commandHandler.executeCommand(client, command);
    }

    private Socket waitConnection() {
        try {
            Socket client;
            client = serverSocket.accept();
            System.out.println("Подключился клиент.");
            return client;
        } catch (IOException e) {
            System.err.printf("Не удалось установить соединение с клиентом: %s", e.getMessage());
            return null;
        }
    }

    public void stopServer() {
        isClosing = true;
        clientThreads.interrupt();
        //System.exit(0);
    }

    void deauth(Client client) {
        commandHandler.deauth(client);
    }

    void showPlayer(Client client, Human player) {
        clients.stream().filter(c -> c != client).filter(c -> c.isAuthorized())
                .forEach(c -> c.sendMessage(Actions.ADDPLAYER, player.getName()+"^", player));
    }

    void movPlayer(Client client, Moves move) {
        client.getHuman().move(move);
        clients.stream().filter(c -> c != client).filter(c -> c.isAuthorized())
                .forEach(c -> c.sendMessage(Actions.MOVPLAYER, move+"^"+client.getHuman().getName()));
    }

    void tpPlayer(Client client, double x, double y) {
        client.getHuman().teleportOther(x, y);
        clients.stream().filter(c -> c != client).filter(c -> c.isAuthorized())
                .forEach(c -> c.sendMessage(Actions.TELEPORT, x+" "+y+"^"+client.getHuman().getName()));
    }

    void shootFromClient(Client client) {
        client.getHuman().shoot();
        clients.stream().filter(c -> c != client).filter(c -> c.isAuthorized())
                .forEach(c -> c.sendMessage(Actions.SHOOT, client.getHuman().getName()));
    }

    void loadPLRS(Client client) {
        clients.stream().filter(c -> c.getHuman() != null)
                .forEach(c -> client.sendMessage(Actions.LOADPLAYERS, c.getHuman().getName() + "^", c.getHuman()));
    }

    void loadPDLS(Client client) {
        puddles.stream().forEach(c -> client.sendMessage(Actions.LOADPUDDLE, c.toString()));
    }

    void rotarePLR(Client client, String move) {
        client.getHuman().setLastMove(Moves.valueOf(move.toUpperCase()));
        clients.stream().filter(c -> c != client).filter(c -> c.isAuthorized())
                .forEach(c -> c.sendMessage(Actions.ROTARE, client.getHuman().getName() + "^" + move));
    }

    void hidePlayer(String player) {
        clients.stream().filter(c -> c.isAuthorized())
                .forEach(c -> c.sendMessage(Actions.REMPLAYER, player));
    }

    void killPlayer(String player) {
        clients.stream().filter(c -> c.isAuthorized())
                .forEach(c -> c.sendMessage(Actions.KILLPLAYER, player));
    }

    void addNewPerson(Client client, Human human) {
        getDBC().addPersonToDB(client.getUserName(), human);
        world.addNewHuman(human);
        client.addHuman(human);
    }

    void remClient(Client client) {
        clients.remove(client);
    }

    public void sendToAllClients(String str, Client client) {
        if (client != null)
            clients.stream()
                    .filter(c -> c.isAuthorized())
                    .forEach(c -> c.sendMessage(Actions.SEND, "" + client.getUserName() + ": " + str + "\n"));
        else
            clients.stream()
                    .filter(c -> c.isAuthorized())
                    .forEach(c -> c.sendMessage(Actions.SEND, "SERVER_MESSAGE -> "+ str + "\n"));
    }


    WorldManager getWorld() {
        return world;
    }

    List<Client> getClients() {
        return clients;
    }

    public boolean hasPlayers() {
        return !clients.isEmpty();
    }

    public DataBaseConnection getDBC() {
        return dataBaseConnection;
    }

    public boolean isClosing() {
        return isClosing;
    }

    public void removePerson(String key, String username) {
        getDBC().removePerson(key, username);
        getWorld().removeHuman(key);
    }
}

