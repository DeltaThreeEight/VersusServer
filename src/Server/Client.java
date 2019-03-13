package Server;


import Entities.Human;

import java.io.*;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

public class Client {
    private static volatile int id = 0;
    private String name;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private Socket client;
    private Thread thread;

    private Human human;
    private String key;

    public Client(SocketChannel channel) {
        name = ""+id++;
        client = channel.socket();
        try {
            InputStream inputClientStream = client.getInputStream();
            OutputStream outClientStream = client.getOutputStream();
            reader = new ObjectInputStream(inputClientStream);
            writer = new ObjectOutputStream(outClientStream);
        } catch (IOException e) {
            System.out.println("Невозможно получить поток ввода!");
        }
    }

    public void interrupt() {
        thread.interrupt();
    }

    public void startService() {
        thread = new Thread(this::servClient);
        thread.start();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    private void servClient() {
        try {
            String command = "";
            Server.loadPLRS(this);

            ClientCommandHandler cmdHandler = new ClientCommandHandler(this);

            cmdHandler.executeCommand("help");

            try {
                while (!command.equals("exit") && !thread.isInterrupted()) {
                    sendMessage(cActions.SEND, "Введите команду\n");
                    command = readLine();
                    System.out.print("Клиент " + name + ": " + command + "\n");
                    if (command == null) break;
                    cmdHandler.executeCommand(command);
                }
            } catch (ClosedByInterruptException e) {
                sendMessage(cActions.SEND, "Сервер закрывает соединение...\n");
                client.close();
                System.out.println("Соединение разорвано с клиентом " + name + ".");
                return;
            } catch (IOException e) {
                System.out.println("Потеряно соединение с клиентом " + name + ".");
                if (getKey() != null) Server.remPlayer(getKey());
                Server.getClients().remove(this);
                return;
            }

            if (getKey() != null) Server.remPlayer(getKey());
            Server.getClients().remove(this);
            System.out.println("Клиент " + name + " отключился.");
        } catch (Exception e) {
            System.out.println("Потеряно соединение с клиентом" + name);
            if (getKey() != null) Server.remPlayer(getKey());
            Server.getClients().remove(this);
        }
    }

    public void sendMessage(cActions action, String str) {
        try {
            writer.writeUTF(action + "^" + str);
            writer.flush();
        } catch (IOException e) {

        }
    }

    public void sendMessage(cActions action, String str, Object human) {
        try {
            writer.writeUTF(action + "^" + str);
            writer.flush();
            sendObject(human);
        } catch (IOException e) {

        }
    }

    private void sendObject(Object obj) {
        try {
            writer.writeObject(obj);
            writer.flush();
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
        return reader.readUTF();
    }

    public Object readObject() {
        try {
            return reader.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setHuman(Human human) {
        this.human = human;
    }

    public Human getHuman() {
        return human;
    }

}
