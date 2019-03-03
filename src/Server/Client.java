package Server;

import Entities.Creature;

import java.io.*;
import java.net.Socket;

public class Client {
    private static int id = 0;
    private String name;
    private BufferedReader reader;
    private DataOutputStream writer;
    private Socket client;
    private Thread thread;

    private Creature creature;

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
            writeUTF("Сервер закрывает соединие...");
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
                command = readLine();
                System.out.print("Клиент " +name+": " + command + "\n");
                cmdHandler.executeCommand(command);
            }
        } catch (IOException e) {
            System.out.println("Потеряно соединение с клиентом " +name+".");
            return;
        }

        System.out.println("\nКлиент "+name+" отключился.");
    }

    public void writeUTF(String str) throws IOException {
        writer.writeUTF(str);
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

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public Creature getCreature() {
        return creature;
    }

}
