package Server;

import Entities.Creature;

import java.io.*;
import java.net.Socket;

public class Client {
    private BufferedReader reader;
    private DataOutputStream writer;
    private Socket client;

    private Creature creature;

    public Client(Socket socket) {
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
            reader.close();
            writer.close();
            client.close();
        } catch (IOException e) {

        }
    }

    public void writeUTF(String str) throws IOException {
        writer.writeUTF(str);
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
