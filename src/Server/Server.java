package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
    private ServerSocket serverSocket = null;
    private ArrayList<Client> clients = new ArrayList<Client>();

    public void run() {
        System.out.println("Попытка запустить сервер...");
        try {
            serverSocket = new ServerSocket(6666);
            System.out.println("Сервер запущен!");
        } catch (IOException e) {
            System.out.println("Не очень хорошие проблемы... Прекращаю выполнение!");
            System.exit(-1);
        }

        while (true) {
            Socket client = waitConnection();
            clients.add(new Client(client));
            //new Thread(() -> servClient(client));
            servClient(client);
        }
    }

    public void stopServer() {
        for (Client c : clients)
            c.closeConnection();
        System.exit(0);
    }

    private void servClient(Socket client) {

        Client client1 = new Client(client);
        String command = "";

        ClientCommandHandler cmdHandler = new ClientCommandHandler(client1);

        cmdHandler.executeCommand("help");

        try {
            while (!command.equals("exit")) {
                command = client1.readLine();
                System.out.print("Клиент: " + command + "\n");
                cmdHandler.executeCommand(command);
            }
        } catch (IOException e) {
            System.out.println("Потеряно соединение с клиентом.");
            return;
        }

        System.out.println("\nКлиент отключился.");
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
