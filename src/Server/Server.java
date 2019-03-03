package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
    private ServerSocket serverSocket = null;
    private ArrayList<Client> clients = new ArrayList<Client>();

    public void run() {
        System.out.println("Попытка запустить сервер...");
        try {
            serverSocket = new ServerSocket(6666,6666, InetAddress.getByName("26.17.59.67"));
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

    public void stopServer() {
        for (Client c : clients)
            c.closeConnection();
        System.exit(0);
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
