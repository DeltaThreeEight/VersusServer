package Server;

import Entities.Human;
import Entities.Merc;
import Entities.Spy;
import World.Location;
import World.WorldManager;
import com.lambdaworks.codec.Base64;
import com.lambdaworks.crypto.SCrypt;
import com.lambdaworks.crypto.SCryptUtil;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.SECONDS;

public class DataBaseConnection {
    private Connection connection = null;
    private WorldManager wrld = null;
    private PreparedStatement loadPersons;
    private PreparedStatement addToDB;
    private PreparedStatement updatePersons;
    private PreparedStatement executeLogin;
    private PreparedStatement checkAuthToken;
    private PreparedStatement updateAuthToken;
    private PreparedStatement checkRegToken;
    private PreparedStatement updateRegToken1;
    private PreparedStatement updateRegToken2;
    private PreparedStatement setAuthToken;
    private PreparedStatement confirmRegister;
    private PreparedStatement removePerson;
    private PreparedStatement loadPlayerPersons;
    private PreparedStatement executeRegister1;
    private PreparedStatement executeRegister2;
    private PreparedStatement getUserSalt;

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
            {
                loadPersons = connection.prepareStatement("SELECT * FROM persons;");
                addToDB = connection.prepareStatement("INSERT INTO persons VALUES (?, ?, ?, ?, ?, ?)");
                updatePersons = connection.prepareStatement("UPDATE persons SET x = ?, y = ? WHERE username=? AND name=?;");
                executeLogin = connection.prepareStatement("SELECT * FROM users WHERE username=? AND  pass=? AND email_conf=?;");
                checkAuthToken = connection.prepareStatement("SELECT * FROM user_tokens WHERE username=? AND  auth_token=?;");
                updateAuthToken = connection.prepareStatement("UPDATE user_tokens SET auth_token_time=? WHERE username=?;");
                checkRegToken = connection.prepareStatement("SELECT * FROM user_tokens WHERE username=? AND reg_token=?;");
                updateRegToken1 = connection.prepareStatement("SELECT * FROM users WHERE username=?;");
                updateRegToken2 = connection.prepareStatement("UPDATE user_tokens SET reg_token=?, reg_token_time=? WHERE username=?;");
                setAuthToken = connection.prepareStatement("UPDATE user_tokens SET auth_token=?, auth_token_time=? WHERE username=?;");
                confirmRegister = connection.prepareStatement("UPDATE users SET email_conf='TRUE' WHERE username=?;");
                removePerson = connection.prepareStatement("DELETE FROM persons WHERE name=? AND username=?;");
                loadPlayerPersons = connection.prepareStatement("SELECT * FROM persons WHERE username=?;");
                executeRegister1 = connection.prepareStatement("INSERT INTO users VALUES (?, ?, ?, 'FALSE', ?);");
                executeRegister2 = connection.prepareStatement("INSERT INTO user_tokens VALUES (?, ?, ?);");
                getUserSalt = connection.prepareStatement("SELECT salt FROM users WHERE username=?;");
            }
        } catch (Exception e) {
            System.out.println("Не удалось подключиться к БД");
        }
    }

    int loadPersons() {
        try {
            int i = 0;
            ResultSet result = loadPersons.executeQuery();
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
                wrld.addNewHuman(name, hum, username);
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
            addToDB.setString(1, human.getName());
            addToDB.setString(2, human.getClass().toString().replace("class Entities.", ""));
            addToDB.setBigDecimal(3, new BigDecimal(human.getLocation().getX()));
            addToDB.setBigDecimal(4, new BigDecimal(human.getLocation().getY()));
            addToDB.setString(5, username);
            addToDB.setTimestamp(6, Timestamp.valueOf(human.getDate()));
            addToDB.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка при добавлении в БД персонажа");
        }
    }

    void updatePersons() {
        try {
            for (Human h : wrld.getHumans().values()) {
                updatePersons.setBigDecimal(1, new BigDecimal(h.getLocation().getX()));
                updatePersons.setBigDecimal(2, new BigDecimal(h.getLocation().getY()));
                updatePersons.setString(3, h.getUser());
                updatePersons.setString(4, h.getName());
                updatePersons.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении персонажей в БД");
        }
    }

    int executeLogin(String login, String pass) {
        try {
            getUserSalt.setString(1, login);
            ResultSet resultSet = getUserSalt.executeQuery();
            if (!resultSet.next()) return 2;
            String salt = resultSet.getString("salt");
            String hash = getHash(pass, salt);
            executeLogin.setString(1, login);
            executeLogin.setString(2, hash);
            executeLogin.setBoolean(3, true);
            ResultSet result = executeLogin.executeQuery();
            // Если правильный логин и пароль
            if (result.next()) return 0;
            executeLogin.setBoolean(3, false);
            ResultSet result2 = executeLogin.executeQuery();
            // Если правильный логин и пароль, но почта не подтверждена
            if (result2.next()) return 1;
            // Неправильный логин и пароль
            return 2;
        } catch (Exception e) {
            System.err.println("Ошибка логина");
            return -1;
        }
    }

    boolean checkAuthToken(Client client, String user, String token) {
        try {
            checkAuthToken.setString(1, user);
            checkAuthToken.setString(2, token);
            ResultSet result = checkAuthToken.executeQuery();
            if (result.next()) {
                LocalDateTime last_login = LocalDateTime.parse(result.getString("auth_token_time").replace(" ", "T"));
                LocalDateTime now = LocalDateTime.now();
                long time = Duration.between(last_login, now).get(SECONDS);
                if (time > 90) {
                    client.sendMessage(cActions.ALERT, "EXPIRED_TOKEN");
                    client.getCmdHandler().deauth();
                } else {
                    updateAuthToken.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    updateAuthToken.setString(2, user);
                    updateAuthToken.executeUpdate();
                    return true;
                }
            } else client.sendMessage(cActions.ALERT, "INVALID_TOKEN");
        } catch (Exception e) {
            System.err.println("Ошибка при проверке токена");
        }
        return false;
    }

    void checkRegToken(Client client, String user, String token) {
        try {
            checkRegToken.setString(1, user);
            checkRegToken.setString(2, token);
            ResultSet result = checkRegToken.executeQuery();
            if (result.next()) {
                LocalDateTime reg_time = LocalDateTime.parse(result.getString("reg_token_time").replace(" ", "T"));
                LocalDateTime now = LocalDateTime.now();
                long time = Duration.between(reg_time, now).get(SECONDS);
                if (time > 120) {
                    client.sendMessage(cActions.ALERT, "EXPIRED_REG_TOKEN");
                    updateRegToken1.setString(1, user);
                    ResultSet res = updateRegToken1.executeQuery();
                    res.next();
                    String mail = res.getString("email");
                    String new_token = getToken();
                    LocalDateTime new_time = LocalDateTime.now();
                    updateRegToken2.setString(1, new_token);
                    updateRegToken2.setTimestamp(2, Timestamp.valueOf(new_time));
                    updateRegToken2.setString(3, user);
                    updateRegToken2.executeUpdate();
                    new Thread(() -> JavaMail.registration(mail, new_token)).start();
                } else {
                    confirmRegister(user);
                    client.sendMessage(cActions.ALERT, "EMAIL_CONF");
                    client.setUserName(user);
                    client.getServer().loadPLRS(client);
                    client.getCmdHandler().setAuthToken(client);
                    client.getServer().sendToAllClients(client.getUserName()+ " AUTHORIZED", null);
                    client.getServer().getDBC().loadPersons(client);
                }
            } else client.sendMessage(cActions.ALERT, "WRONG_TOKEN");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка при проверке регистрационного токена");
        }
    }

    void setAuthToken(String username, String token) {
        try {
            setAuthToken.setString(1, token);
            setAuthToken.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            setAuthToken.setString(3, username);
            setAuthToken.executeUpdate();
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении авторизационного токена");
        }
    }

    private void confirmRegister(String username) {
        try {
            confirmRegister.setString(1, username);
            confirmRegister.executeUpdate();
        } catch (Exception e) {
            System.err.println("Ошибка при подтвеждении регистрации");
        }
    }

    public void removePerson(String username, String name) {
        try {
            removePerson.setString(1, name);
            removePerson.setString(2, username);
            removePerson.executeUpdate();
        } catch (Exception e) {
            System.err.println("Ошибка при удалении персонажа");
        }
    }

    void loadPersons(Client client) {
        try {
            loadPlayerPersons.setString(1, client.getUserName());
            ResultSet result = loadPlayerPersons.executeQuery();
            while (result.next()) {
                Human person = WorldManager.getInstance().getHuman(result.getString("name"));
                client.addHuman(person);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке персонажей");
        }
    }

    boolean executeRegister(String login, String mail, String pass) {
        try {
            String salt = getSalt();
            String hash = getHash(pass, salt);
            executeRegister1.setString(1, login);
            executeRegister1.setString(2, mail);
            executeRegister1.setString(3, hash);
            executeRegister1.setString(4, salt);
            executeRegister1.executeUpdate();
            String reg_token = getToken();
            executeRegister2.setString(1, login);
            executeRegister2.setString(2, reg_token);
            executeRegister2.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            executeRegister2.executeUpdate();
            new Thread(() -> JavaMail.registration(mail, reg_token)).start();
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при регистрации");
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

    static String getHash(String pass, String salt) {
        try {
            byte[] hash = SCrypt.scrypt(pass.getBytes("UTF-8"), salt.getBytes("UTF-8"), 16, 16, 16, 32);
            return new String(Base64.encode(hash));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static String getSalt() {
        try {
            byte[] salt = new byte[16];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
            return new String(Base64.encode(salt));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
