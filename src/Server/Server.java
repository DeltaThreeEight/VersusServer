package Server;

import Entities.Human;
import Entities.Moves;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Thread {
    private ServerSocket serverSocket = null;
    private volatile List<Client> clients = new CopyOnWriteArrayList<Client>();
    private int port = 8901;
    private String host = "localhost";
    private DataBaseConnection dataBaseConnection = null;

    public Server() {
    }

    public Server(int port) {
        this.port = port;
    }

    public Server(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void run() {
        System.out.println("Попытка запустить сервер на "+host+":"+port+ "...");
        try {
            serverSocket = new ServerSocket(port, 100, InetAddress.getByName(host));
            System.out.println("Сервер запущен! Адрес: "+serverSocket.getInetAddress());
            System.out.println("Порт: "+serverSocket.getLocalPort());
            System.out.println("\nИнициализация соединения с БД...");
            dataBaseConnection = new DataBaseConnection();
            System.out.println("Загрузка персонажей...");
            System.out.println("Загружено " + dataBaseConnection.loadPersons() + " персонажей.");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                dataBaseConnection.updatePersons();
            }));
        } catch (IOException e) {
            System.out.println("Не очень хорошие проблемы... Прекращаю выполнение!");
            System.exit(-1);
        }

        while (true) {
            Client client = new Client(waitConnection(), this);
            clients.add(client);
            client.startService();
        }
    }

    public void addPlayer(Client client, String key, Human player) {
        clients.stream().filter(c -> c != client)
                .forEach(c -> c.sendMessage(cActions.ADDPLAYER, key+"^", player));
    }

    public void movPlayer(Client client, String key, Moves move) {
        clients.stream().filter(c -> c != client)
                .forEach(c -> c.sendMessage(cActions.MOVPLAYER, move+"^"+key));
    }

    public void loadPLRS(Client client) {
        clients.stream().filter(c -> c.getKey() != null)
                .forEach(c -> client.sendMessage(cActions.LOADPLR, c.getKey() + "^", c.getHuman()));
    }

    public void remPlayer(String player) {
        clients.stream().forEach(c -> c.sendMessage(cActions.REMPLAYER, player));
    }

    public void remClient(Client client) {
        clients.remove(client);
    }

    public void sendToAllClients(String str, Client client) {
        if (client != null)
            clients.stream()
                    .filter(c -> c.getIsAuth())
                    .forEach(c -> c.sendMessage(cActions.SEND, "" + client.getUserName() + ": " + str + "\n"));
        else
            clients.stream()
                    .filter(c -> c.getIsAuth())
                    .forEach(c -> c.sendMessage(cActions.SEND, "Сообщение от сервера -> "+ str + "\n"));
    }

    public void stopServer() {
        clients.stream().forEach(c -> c.closeConnection());
        System.exit(0);
    }

    public List<Client> getClients() {
        return clients;
    }

    public boolean hasPlayers() {
        return !clients.isEmpty();
    }

    public DataBaseConnection getDBC() {
        return dataBaseConnection;
    }

    private Socket waitConnection() {
        try {
            Socket client;
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

class JavaMail {
    static final String ENCODING = "UTF-8";

    public static void registration(String email, String reg_token){
        String subject = "Confirm registration";
        String content = "Registration token: "+reg_token;
        String smtpHost="mail.buycow.org";
        String from="makailyn.talei@buycow.org";
        String login="makailyn.talei";
        String password="Login1";
        String smtpPort="25";
        try {
            sendSimpleMessage(login, password, from, email, content, subject, smtpPort, smtpHost);
        } catch (Exception e) {
            System.out.println("Не удалось отправить письмо");
        }
    }

    public static void main(String args[]) throws MessagingException, UnsupportedEncodingException {
        String subject = "Confirm registration";
        String content = "Click on the link...";
        String smtpHost="mail.buycow.org";
        String from="makailyn.talei@buycow.org";
        String to = "mccabe.jireh@buycow.org";
        String login="makailyn.talei";
        String password="Login1";
        String smtpPort="25";
        sendSimpleMessage (login, password, from, to, content, subject, smtpPort, smtpHost);
    }

    public static void sendSimpleMessage(String login, String password, String from, String to, String content, String subject, String smtpPort, String smtpHost)
            throws MessagingException, UnsupportedEncodingException {
        Authenticator auth = new MyAuthenticator(login, password);

        Properties props = System.getProperties();
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.mime.charset", ENCODING);
        Session session = Session.getDefaultInstance(props, auth);

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setText(content);
        Transport.send(msg);
    }
}

class MyAuthenticator extends Authenticator {
    private String user;
    private String password;

    MyAuthenticator(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        String user = this.user;
        String password = this.password;
        return new PasswordAuthentication(user, password);
    }
}
