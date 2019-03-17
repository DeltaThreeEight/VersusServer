package Server;

import Entities.Human;
import Entities.Moves;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Thread {
    private ServerSocket serverSocket = null;
    private static volatile List<Client> clients = new CopyOnWriteArrayList<Client>();
    private int port = 8900;
    private String host = "localhost";

    public Server() {
    }

    public Server(int port) {
        this.port = port;
    }

    public Server(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void run() {
        System.out.println("Попытка запустить сервер на "+host+":"+port+ "...");
        try {
            serverSocket = new ServerSocket(port, 100, InetAddress.getByName(host));
            System.out.println("Сервер запущен! Адрес: "+serverSocket.getInetAddress());
            System.out.println("Порт: "+serverSocket.getLocalPort());
        } catch (IOException e) {
            System.out.println("Не очень хорошие проблемы... Прекращаю выполнение!");
            System.exit(-1);
        }

        while (true) {
            Client client = new Client(waitConnection());
            clients.add(client);
            client.startService();
        }
    }

    public static void addPlayer(Client client, String key, Human player) {
        clients.stream().filter(c -> c != client)
                .forEach(c -> c.sendMessage(cActions.ADDPLAYER, key+"^", player));
    }

    public static void movPlayer(Client client, String key, Moves move) {
        clients.stream().filter(c -> c != client)
                .forEach(c -> c.sendMessage(cActions.MOVPLAYER, move+"^"+key));
    }

    public static void loadPLRS(Client client) {
        clients.stream().filter(c -> c.getKey() != null)
                .forEach(c -> client.sendMessage(cActions.LOADPLR, c.getKey() + "^", c.getHuman()));
    }

    public static void remPlayer(String player) {
        clients.stream().forEach(c -> c.sendMessage(cActions.REMPLAYER, player));
    }

    public static void remClient(Client client) {
        clients.remove(client);
    }

    public static void sendToAllClients(String str, Client client) {
        if (client != null)
            clients.stream().forEach(c -> c.sendMessage(cActions.SEND, "" + client.getName() + ": " + str + "\n"));
        else
            clients.stream().forEach(c -> c.sendMessage(cActions.SEND, "Сообщение от сервера -> "+ str + "\n"));
    }

    public void stopServer() {
        clients.stream().forEach(c -> c.closeConnection());
        System.exit(0);
    }

    public static List<Client> getClients() {
        return clients;
    }

    public static boolean hasPlayers() {
        return !clients.isEmpty();
    }

    private Socket waitConnection() {
        try {
            Socket client;
            System.out.println("Ждём нового соединения...");
            client = serverSocket.accept();
            System.out.println("Подключение успешно.");
            return client;
        } catch (IOException e) {
            System.out.println("Что то не так");
            return null;
        }
    }
}
