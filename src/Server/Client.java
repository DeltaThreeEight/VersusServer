package Server;


import Entities.Human;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Client {
    private static volatile int id = 0;
    private String userName;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private Socket client;
    private Thread thread;
    private boolean isAuth = false;
    private HashMap<String, Human> persons = new HashMap();
    private Server server;

    private Human human;
    private String key;

    public Client(Socket socket, Server server) {
        userName = ""+id++;
        client = socket;
        this.server = server;
        try {
            InputStream inputClientStream = client.getInputStream();
            OutputStream outClientStream = client.getOutputStream();
            reader = new ObjectInputStream(inputClientStream);
            writer = new ObjectOutputStream(outClientStream);
        } catch (IOException e) {
            System.out.println("Невозможно получить поток ввода!");
        }
    }

    public void setIsAuth(boolean a) {
        isAuth = a;
        if (isAuth) {

        }
    }

    public boolean getIsAuth() {
        return isAuth;
    }

    public void closeConnection() {
        try {
            sendMessage(cActions.SEND, "Сервер закрывает соединение...\n");
            writer.close();
            client.close();
        } catch (IOException e) {
        }
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
            server.loadPLRS(this);

            ClientCommandHandler cmdHandler = new ClientCommandHandler(this , server);

            try {
                while (!command.equals("exit")) {
                    sendMessage(cActions.SEND, "Введите команду\n");
                    command = readLine();
                    System.out.print("Клиент " + userName + ": " + command + "\n");
                    if (command == null) break;
                    cmdHandler.executeCommand(command);
                }
            } catch (IOException e) {
                System.out.println("Потеряно соединение с клиентом " + userName + ".");
                if (getKey() != null) server.remPlayer(getKey());
                server.getClients().remove(this);
                return;
            }

            if (getKey() != null) server.remPlayer(getKey());
            server.getClients().remove(this);
            System.out.println("Клиент " + userName + " отключился.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Потеряно соединение с клиентом" + userName);
            if (getKey() != null) server.remPlayer(getKey());
            server.getClients().remove(this);
        }
        finally {
            server.remClient(this);
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public void showHumans() {
        sendMessage(cActions.SEND,"Список ваших персонажей:\n");
        persons.values().stream().map(human -> human.getName()).forEach(c -> sendMessage(cActions.SEND, c+"\n"));
    }

    public void addHuman(String key,Human human) {
        persons.put(key, human);
    }

    public HashMap<String, Human> getPersons() {
        return persons;
    }

}
