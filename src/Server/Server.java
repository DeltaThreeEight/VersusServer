package Server;

import Entities.Human;
import Entities.Moves;
import IOStuff.MyDeserialize;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server extends Thread {
    private ServerSocket serverSocket = null;
    private static volatile List<Client> clients = Collections.synchronizedList(new ArrayList<Client>());

    public void run() {
        System.out.println("Попытка запустить сервер...");
        try {
            serverSocket = new ServerSocket(666,666, InetAddress.getByName(null));
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
        for (Client c : clients) {
            if (c != client) {
                c.sendMessage(cActions.ADDPLAYER, key+"^");
                c.sendObject(player);
            }
        }
    }

    public static void movPlayer(Client client, String key, Moves move) {
        for (Client c : clients) {
            if (c != client) {
                c.sendMessage(cActions.MOVPLAYER, move+"^"+key);
            }
        }
    }

    public static void loadPLRS(Client client) {
        for (Client c : clients) {
            if (c.getKey() != null) {
                client.sendMessage(cActions.LOADPLR, c.getKey() + "^");
                client.sendObject(c.getHuman());
            }

        }
    }

    public static void remPlayer(String player) {
        for (Client c : clients) {
                c.sendMessage(cActions.REMPLAYER, player);
        }
    }

    public static void sendToAllClients(String str, Client client) {
        if (client != null) {
            for (Client c : clients) {
                c.sendMessage(cActions.SEND, "" + client.getName() + ": " + str + "\n");
            }
        }
        else for (Client c : clients) {
            c.sendMessage(cActions.SEND, "Сообщение от сервера -> "+ str + "\n");
        }
    }

    public void stopServer() {
        for (Client c : clients)
            c.closeConnection();
        System.exit(0);
    }

    public static List<Client> getClients() {
        return clients;
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
