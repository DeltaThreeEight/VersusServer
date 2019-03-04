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

public class Server extends Thread {
    static GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Human.class, new MyDeserialize());
    static Gson gson = builder.create();
    private ServerSocket serverSocket = null;
    private static ArrayList<Client> clients = new ArrayList<Client>();

    public void run() {
        System.out.println("Попытка запустить сервер...");
        try {
            serverSocket = new ServerSocket(8900,8900, InetAddress.getByName("26.17.59.67"));
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

    public static void addPlayer(Client client, Human player) {
        for (Client c : clients) {
            if (c != client) {
                c.sendMessage(cActions.ADDPLAYER, gson.toJson(player, Human.class));
            }
        }
    }

    public static void movPlayer(Client client, Human player, Moves move) {
        for (Client c : clients) {
            if (c != client) {
                c.sendMessage(cActions.MOVPLAYER, move+"*"+gson.toJson(player, Human.class));
            }
        }
    }

    public static void remPlayer(Human player) {
        for (Client c : clients) {
                c.sendMessage(cActions.REMPLAYER, gson.toJson(player, Human.class));
        }
    }

    public static void sendToAllClients(String str, Client client) {
        if (client != null) {
            for (Client c : clients) {
                c.sendMessage(cActions.SEND, "\n" + client.getName() + ": " + str + "     ");
            }
        }
        else for (Client c : clients) {
            c.sendMessage(cActions.SEND, "\nСообщение от сервера -> "+ str + "     ");
        }
    }

    public void stopServer() {
        for (Client c : clients)
            c.closeConnection();
        System.exit(0);
    }

    public static ArrayList<Client> getClients() {
        return clients;
    }

    private Socket waitConnection() {
        try {
            Socket client = null;
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
