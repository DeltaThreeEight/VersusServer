package Server;


import Entities.Human;
import Server.Commands.ClientCommand;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable {
    private static volatile AtomicInteger idGenerator = new AtomicInteger(0);
    private String userName;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private Socket clientSocket;
    private boolean isAuth = false;
    private boolean isTokenValid = true;
    private HashMap<String, Human> persons = new HashMap<>();
    private Server server;
    private int id;
    private boolean reloading = false;

    private Human human;

    Client(Socket socket, Server server) {
        id = idGenerator.getAndIncrement();
        userName = id+"";

        clientSocket = socket;
        this.server = server;

        try {

            InputStream inputClientStream = clientSocket.getInputStream();
            OutputStream outClientStream = clientSocket.getOutputStream();

            reader = new ObjectInputStream(inputClientStream);
            writer = new ObjectOutputStream(outClientStream);

        } catch (IOException e) {
            System.err.printf("Ошибка при подключении клиента %s: %s\n", userName, e.getMessage());
        }
    }

    public boolean isReloading() {
        return reloading;
    }

    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    void setAuthorized(boolean authorized) {
        isAuth = authorized;
        isTokenValid = authorized;
        if (!authorized) {
            human = null;
            userName = id+"";
            persons.clear();
            sendMessage(Actions.DEAUTH, null);
        }
    }

    boolean isTokenValid() {
        return isTokenValid;
    }

    boolean isAuthorized() {
        return isAuth;
    }

    void closeConnection() {
        try {
            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.printf("Соединение с клиентом %s оборвалось при попытке закрыть соединение: %s\n", userName, e.getMessage());
        }
    }

    int getId() {
        return id;
    }

    Server getServer() {
        return server;
    }

    public void run() {
        try {

            ClientCommand cmd = readCMD();

            while (!cmd.getCommandName().equals("exit")) {
                System.out.printf("Клиент %s: %s\n", userName, cmd.getCommandName());
                server.executeClientCommand(this, cmd);
                cmd = readCMD();
            }

            System.out.printf("Клиент %s отключился.\n", userName);

        } catch (Exception e) {
            if (!server.isClosing()) {
                System.err.printf("Потеряно соединение с клиентом %s: %s\n", userName, e.getMessage());
            }
        } finally {
            if (human != null)
                server.hidePlayer(human.getName());
            server.remClient(this);
        }
    }

    public void sendMessage(Actions action, String str) {
        try {
            writer.writeUTF(action + "^" + str);
            writer.flush();
        } catch (IOException e) {
            if (!server.isClosing())
                System.err.printf("Ошибка при отправке сообщения клиенту %s: %s\n", userName, e.getMessage());
        }
    }

    public void sendMessage(Actions action, String str, Object human) {
        try {
            writer.writeUTF(action + "^" + str);
            writer.flush();
            sendObject(human);
        } catch (IOException e) {
            System.out.printf("Ошибка при отправке объекта клиенту %s: %s\n", e.getMessage());
        }
    }

    private void sendObject(Object obj) throws IOException {
        writer.writeObject(obj);
        writer.flush();
        writer.reset();
    }

    String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    ClientCommand readCMD() {
        try {
            return (ClientCommand) reader.readObject();
        } catch (Exception e) {
            System.err.printf("Ошибка при чтении команды от клиента %s: %s\n", userName, e.getMessage());
            return null;
        }
    }

    Human readHuman() {
        try {
            return (Human) reader.readObject();
        } catch (Exception e) {
            System.err.printf("Ошибка при приёме персонажа от клиента %s: %s\n", userName, e.getMessage());
            return null;
        }
    }

    public void setHuman(Human human) {
        if (this.human != null)
            server.hidePlayer(this.human.getName());

        this.human = human;

        if (human != null) {
            sendMessage(Actions.ALERT, "PERSON_SELECTED " + human.getName());
            sendMessage(Actions.DESERIALIZE, null, human);
            server.showPlayer(this, human);
        }
    }

    public Human getHuman() {
        return human;
    }

    void showHumans() {
        String[] names = persons.values()
                .stream()
                .map(Human::getName)
                .toArray(String[]::new);

        sendMessage(Actions.SENDPERSONLIST, null, names);
    }

    void addHuman(Human human) {
        persons.put(human.getName(), human);
        showHumans();
    }
    void removeHuman(String key) {
        persons.remove(key);
        server.removePerson(key, userName);
        sendMessage(Actions.ALERT, "PERSON_REMOVED");
        showHumans();
    }

    HashMap<String, Human> getPersons() {
        return persons;
    }

}
