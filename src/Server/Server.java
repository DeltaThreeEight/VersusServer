package Server;

import Entities.Human;
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
            serverSocket = new ServerSocket(8900,8900, InetAddress.getByName("localhost"));
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

    public static void sendToAllClients(String str, Client client) {
        if (client != null) {
            for (Client c : clients) {
                try {
                    c.writeUTF("SEND^" +"\n"+ client.getName() + ": " + str + "\n");
                } catch (IOException e) {
                }
            }
        }
        else for (Client c : clients) {
            try {
                c.writeUTF("SEND^"  + "\nСообщение от сервера -> " + str + "\n");
            } catch (IOException e) {
            }
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
