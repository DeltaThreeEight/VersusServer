package Server;

import Entities.Human;
import Entities.Merc;
import Entities.Spy;
import World.Location;
import World.WorldManager;
import com.lambdaworks.codec.Base64;
import com.lambdaworks.crypto.SCrypt;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.SECONDS;

public class DataBaseConnection {
    private Connection connection = null;
    private WorldManager wrld = null;

    {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Драйвер подключен");

            String url = "jdbc:postgresql://127.0.0.1:5432/";
            String name = "postgres";
            String pass = "Dima13145";
            connection = DriverManager.getConnection(url, name, pass);
            System.out.println("Соединение успешно установлено\n");
            wrld = WorldManager.getInstance();
        } catch (Exception e) {
            System.out.println("Не удалось подключиться к БД");
        }
    }

    int loadPersons() {
        try {
            int i = 0;
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM persons;");
            while (result.next()) {
                String side = result.getString("side");
                String username = result.getString("username");
                String name = result.getString("name");
                double x = result.getDouble("x");
                double y = result.getDouble("y");
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
            return -1;
        }
    }

    public void addToDB(String username, Human human) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO persons VALUES ('" + human.getName() + "', '" + human.getClass().toString().replace("class Entities.", "") + "', '" + human.getLocation().getX() + "', '" + human.getLocation().getY() + "', '" + username + "', '" + human.getDate().toString().replace("T", " ") + "');");
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении в БД персонажа");
        }
    }

    void updatePersons() {
        try {
            for (Human h : wrld.getHumans().values()) {
                Statement statement = connection.createStatement();
                String query = String.format("UPDATE persons SET x =%s, y =%s WHERE username='%s' AND name='%s';", h.getLocation().getX(), h.getLocation().getY(), h.getUser(), h.getName());
                statement.executeUpdate(query);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении персонажей в БД");
        }
    }

    int executeLogin(String login, String pass) {
        try {
            Statement statement = connection.createStatement();
            String query = String.format("SELECT * FROM users WHERE username='%s' AND  pass='%s' AND email_conf=TRUE;", login, pass);
            ResultSet result = statement.executeQuery(query);
            if (result.next()) return 0;
            ResultSet result2 = statement.executeQuery(query);
            if (result2.next()) return 1;
            return 2;
        } catch (Exception e) {
            System.out.println("Ошибка логина");
            return -1;
        }
    }

    boolean checkAuthToken(Client client, String user, String token) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(String.format("SELECT * FROM user_tokens WHERE username='%s' AND  auth_token='%s';", user, token));
            if (result.next()) {
                LocalDateTime reg_time = LocalDateTime.parse(result.getString("auth_token_time").replace(" ", "T"));
                LocalDateTime now = LocalDateTime.now();
                long time = Duration.between(reg_time, now).get(SECONDS);
                if (time > 90) {
                    client.sendMessage(cActions.SEND, "Срок действия токена истёк\n");
                    client.setIsAuth(false);
                    client.setIsTokenValid(false);
                    client.setHuman(null);
                    client.sendMessage(cActions.DEAUTH, null);
                    client.getServer().sendToAllClients(client.getUserName()+ " отключился от сервера.", null);
                } else {
                    statement.executeUpdate(String.format("UPDATE user_tokens SET auth_token_time='%s' WHERE username='%s';", LocalDateTime.now(), user));
                    return true;
                }
            } else client.sendMessage(cActions.SEND, "Неверный токен\n");
        } catch (Exception e) {
            System.out.println("Ошибка при проверке токена");
        }
        return false;
    }

    void checkRegToken(Client client, String user, String token) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(String.format("SELECT * FROM user_tokens WHERE username='%s' AND  reg_token='%s';", user, token));
            if (result.next()) {
                LocalDateTime reg_time = LocalDateTime.parse(result.getString("reg_token_time").replace(" ", "T"));
                LocalDateTime now = LocalDateTime.now();
                long time = Duration.between(reg_time, now).get(SECONDS);
                if (time > 120) {
                    client.sendMessage(cActions.SEND, "Срок действия токена истёк\n" +
                            "На почту будет отправлен новый токен\n");
                    ResultSet res = statement.executeQuery(String.format("SELECT * FROM users WHERE username='%s';", user));
                    res.next();
                    String mail = res.getString("email");
                    String new_token = getToken();
                    LocalDateTime new_time = LocalDateTime.now();
                    Statement statement1 = connection.createStatement();
                    statement1.executeUpdate(String.format("UPDATE user_tokens SET reg_token='%s', reg_token_time='%s' WHERE username='%s';", new_token, new_time, user));
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
            System.out.println("Ошибка при проверке регистрационного токена");
        }
    }

    void setAuthToken(String username, String token) {
        try {
            Statement state = connection.createStatement();
            state.executeUpdate(String.format("UPDATE user_tokens SET auth_token='%s', auth_token_time='%s' WHERE username='%s';", token, LocalDateTime.now(), username));
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении авторизационного токена");
        }
    }

    private void confirmRegister(String username) {
        try {
            Statement state = connection.createStatement();
            state.executeUpdate(String.format("UPDATE users SET email_conf='TRUE' WHERE username='%s';", username));
        } catch (Exception e) {
            System.out.println("Ошибка при подтвеждении регистрации");
        }
    }

    public void removePerson(String username, String name) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(String.format("DELETE FROM persons WHERE name='%s' AND username='%s';", name, username));
        } catch (Exception e) {
            System.out.println("Ошибка при удалении персонажа");
        }
    }

    void loadPersons(Client client) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(String.format("SELECT * FROM persons WHERE username='%s';", client.getUserName()));
            while (result.next()) {
                Human person = WorldManager.getInstance().getHuman(client.getUserName() + result.getString("name"));
                client.addHuman(result.getString("name"), person);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке персонажей");
        }
    }

    boolean executeRegister(String login, String mail, String hash) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(String.format("INSERT INTO users VALUES ('%s', '%s', '%s', 'FALSE');", login, mail, hash));
            String reg_token = DataBaseConnection.getToken();
            statement.executeUpdate(String.format("INSERT INTO user_tokens VALUES ('%s', '%s', '%s');", login, reg_token, LocalDateTime.now()));
            new Thread(() -> JavaMail.registration(mail, reg_token)).start();
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка при регистрации");
            return false;
        }
    }

    static String getToken() {
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
