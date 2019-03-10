package Server;

import Entities.Human;
import Entities.Moves;
import java.io.*;
import java.net.*;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Thread {
    private ServerSocketChannel serverSocket = null;
    private static volatile List<Client> clients = new CopyOnWriteArrayList<Client>();

    public void run() {
        System.out.println("Попытка запустить сервер...");
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(666));
            System.out.println("Сервер запущен! Адрес: "+serverSocket.socket().getInetAddress());
            System.out.println("Порт: "+serverSocket.socket().getLocalPort());
        } catch (IOException e) {
            System.out.println("Не очень хорошие проблемы... Прекращаю выполнение!");
            System.exit(-1);
        }

        while (true) {
            SocketChannel clientChannel = waitConnection();
            if (clientChannel == null) break;
            Client client = new Client(clientChannel);
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

    public static void sendToAllClients(String str, Client client) {
        if (client != null)
            clients.stream().forEach(c -> c.sendMessage(cActions.SEND, "" + client.getName() + ": " + str + "\n"));
        else
            clients.stream().forEach(c -> c.sendMessage(cActions.SEND, "Сообщение от сервера -> "+ str + "\n"));
    }

    public void stopServer() {
        clients.stream().forEach(c -> c.interrupt());
        this.interrupt();
    }

    public static List<Client> getClients() {
        return clients;
    }

    private SocketChannel waitConnection() {
        try {
            SocketChannel client;
            System.out.println("Ждём нового соединения...");
            client = serverSocket.accept();
            System.out.println("Подключение успешно.");
            return client;
        } catch (ClosedByInterruptException e) {
            System.out.println("Сервер завершает свою работу...");
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.out.println("Не удалось закрыть сетевой канал.");
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Что то не так");
            return null;
        }
    }
}
