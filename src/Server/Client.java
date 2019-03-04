package Server;


import Entities.Human;

import java.io.*;
import java.net.Socket;

public class Client {
    private static int id = 0;
    private String name;
    private BufferedReader reader;
    private DataOutputStream writer;
    private Socket client;
    private Thread thread;

    private Human human;

    public Client(Socket socket) {
        name = ""+id++;
        client = socket;
        try {
            InputStream inputClientStream = client.getInputStream();
            OutputStream outClientStream = client.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(inputClientStream));
            writer = new DataOutputStream(outClientStream);
        } catch (IOException e) {
            System.out.println("Невозможно получить поток ввода!");
        }
    }

    public void closeConnection() {
        try {
            sendMessage(cActions.SEND, "\nСервер закрывает соединение...\n");
            writer.close();
            client.close();
        } catch (IOException e) {
        }
    }

    public void startService() {
        thread = new Thread(this::servClient);
        thread.start();
    }

    private void servClient() {

        String command = "";

        ClientCommandHandler cmdHandler = new ClientCommandHandler(this);

        cmdHandler.executeCommand("help");

        try {
            while (!command.equals("exit")) {
                sendMessage(cActions.SEND, "Введите комнаты: ");
                command = readLine();
                System.out.print("Клиент " +name+": " + command + "\n");
                if (command == null) break;
                cmdHandler.executeCommand(command);
            }
        } catch (IOException e) {
            System.out.println("Потеряно соединение с клиентом " +name+".");
            return;
        }

        System.out.println("\nКлиент "+name+" отключился.");
    }

    public void sendMessage(cActions action, String str) {
        try {
            writer.writeUTF(action + "^" + str);
        } catch (IOException e) {

        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String readLine() throws IOException{
        return reader.readLine();
    }

    public void setHuman(Human human) {
        this.human = human;
    }

    public Human getHuman() {
        return human;
    }

}
