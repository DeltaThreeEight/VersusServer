package Server;

import Entities.Human;
import Entities.Merc;
import Entities.Spy;
import World.Location;
import World.WorldManager;
import com.lambdaworks.codec.Base64;
import com.lambdaworks.crypto.SCrypt;
import com.lambdaworks.crypto.SCryptUtil;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;

public class DataBaseConnection {
    private String url = "jdbc:postgresql://127.0.0.1:5432/";
    private String name = "postgres";
    private String pass = "Dima13145";
    private Connection connection = null;
    private WorldManager wrld = null;

    {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Драйвер подключен");

            connection = DriverManager.getConnection(url, name, pass);
            System.out.println("Соединение успешно установлено\n");
            wrld = WorldManager.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addPerson(Human human) {
        return false;
    }

    public int loadPersons() {
        try {
            int i = 0;
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM \"persons\";");
            while (result.next()) {
                String side = result.getString("side");
                String username = result.getString("username");
                String name = result.getString("name");
                Double x = result.getDouble("x");
                Double y = result.getDouble("y");
                LocalDateTime time = LocalDateTime.parse(result.getString("creation_date").replace(" ", "T"));
                Human hum;
                if (side.equals("Spy"))
                    hum = new Spy(name, new Location(x, y), time);
                else hum = new Merc(name, new Location(x, y), time);
                wrld.addNewHuman(username+name, hum, username);
                i++;
            }
            return i;
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке персонажей");
            e.printStackTrace();
            return -1;
        }
    }

    public void addToDB(String username, Human human) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO persons VALUES ('" + human.getName() + "', '" +
                    human.getClass().toString().replace("class Entities.", "") + "', '"
                    +human.getLocation().getX()+"', '"+ human.getLocation().getY()+"', '"
                    + username+"', '"+human.getDate().toString().replace("T", " ") +"');");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePersons() {
        try {
            for (Human h : wrld.getHumans().values()) {
                Statement statement = connection.createStatement();
                String query = "UPDATE persons SET x ="+h.getLocation().getX()+ ", "
                        + "y ="+h.getLocation().getY()+" WHERE username='"+h.getUser()+ "' AND name='"+h.getName()+ "';";
                statement.executeUpdate(query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int executeLogin(String login, String pass) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM users WHERE username='" + login + "' AND "+ " pass='"+pass+"' AND email_conf=TRUE;");
            if (result.next()) return 0;
            ResultSet result2 = statement.executeQuery("SELECT * FROM users WHERE username='" + login + "' AND "+ " pass='"+pass+"' AND email_conf=FALSE ;");
            if (result2.next()) return 1;
            return 2;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean checkAuthToken(Client client, String user, String token) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM user_tokens WHERE username='" + user + "' AND "+ " auth_token='"+token+"';");
            if (result.next()) {
                LocalDateTime reg_time = LocalDateTime.parse(result.getString("auth_token_time").replace(" ", "T"));
                LocalDateTime now = LocalDateTime.now();
                long time = Duration.between(reg_time, now).get(SECONDS);
                if (time > 90) {
                    client.sendMessage(cActions.SEND, "Срок действия токена истёк\n" +
                            "Вам необходимо авторизоваться по новой\n");
                    client.sendMessage(cActions.DEAUTH, null);
                } else {
                    statement.executeUpdate("UPDATE user_tokens SET auth_token_time='"+LocalDateTime.now()+"' WHERE username='"+user+"';");
                    return true;
                }
            } else client.sendMessage(cActions.SEND, "Неверный токен\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void checkRegToken(Client client, String user, String token) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM user_tokens WHERE username='" + user + "' AND "+ " reg_token='"+token+"';");
            if (result.next()) {
                LocalDateTime reg_time = LocalDateTime.parse(result.getString("reg_token_time").replace(" ", "T"));
                LocalDateTime now = LocalDateTime.now();
                long time = Duration.between(reg_time, now).get(SECONDS);
                if (time > 120) {
                    client.sendMessage(cActions.SEND, "Срок действия токена истёк\n" +
                            "На почту будет отправлен новый токен\n");
                    String mail = statement.executeQuery("SELECT * FROM users WHERE username='"+user+"';").getString("email");
                    String new_token = getToken();
                    LocalDateTime new_time = LocalDateTime.now();
                    Statement statement1 = connection.createStatement();
                    statement1.executeUpdate("UPDATE user_tokens SET reg_token='"+new_token+"', reg_token_time='"+new_time+"' WHERE username='"+user+"';");
                    new Thread(() -> JavaMail.registration(mail, new_token)).start();
                } else {
                    confirmRegister(user);
                    client.sendMessage(cActions.SEND, "Почта подтверждена!\n");
                    client.setUserName(user);
                    client.getCmdHandler().setAuthToken(client);
                    client.getServer().sendToAllClients(client.getUserName()+ " авторизовался.", null);
                    client.getServer().getDBC().loadPersons(client);
                }
            } else client.sendMessage(cActions.SEND, "Неверный токен\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAuthToken(String username, String token) {
        try {
            Statement state = connection.createStatement();
            state.executeUpdate("UPDATE user_tokens SET auth_token='"+token+ "', auth_token_time='"+LocalDateTime.now()+  "' WHERE username='"+username+"';");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void confirmRegister(String username) {
        try {
            Statement state = connection.createStatement();
            state.executeUpdate("UPDATE users SET email_conf='TRUE' WHERE username='"+username+"';");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean removePerson(String username, String name) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM persons WHERE name='"+name+"' AND username='"+username+"';");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadPersons(Client client) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM persons WHERE username='" + client.getUserName() + "';");
            while (result.next()) {
                Human person = WorldManager.getInstance().getHuman(client.getUserName() + result.getString("name"));
                client.addHuman(result.getString("name"), person);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean executeRegister(String login, String mail, String hash) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO users VALUES ('" + login + "', '" +mail+ "', '"+hash+"', 'FALSE"+"');");
            String reg_token = DataBaseConnection.getToken();
            statement.executeUpdate("INSERT INTO user_tokens VALUES ('" + login + "', '" +reg_token+ "', '"+LocalDateTime.now()+"');");
            new Thread(() -> JavaMail.registration(mail, reg_token)).start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getToken() {
        try {
            byte[] str = new byte[32];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(str);
            byte[] derived = SCrypt.scrypt(str, "string".getBytes(), 16, 16, 16, 32);
            return new String(Base64.encode(derived));
        } catch (Exception e) {
            return null;
        }
    }
}
