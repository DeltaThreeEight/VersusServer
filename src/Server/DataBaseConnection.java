package Server;

import Entities.Human;
import Entities.Merc;
import Entities.Spy;
import World.Location;
import World.WorldManager;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.*;

public class DataBaseConnection {
    private Connection connection;
    private WorldManager world;
    private PreparedStatement loadPersons;
    private PreparedStatement addPersonToDB;
    private PreparedStatement updatePersons;
    private PreparedStatement executeLogin;
    private PreparedStatement checkRegToken;
    private PreparedStatement getEmail;
    private PreparedStatement updateRegToken;
    private PreparedStatement confirmRegister;
    private PreparedStatement removePerson;
    private PreparedStatement loadPlayerPersons;
    private PreparedStatement executeRegister;
    private PreparedStatement setRegistrationToken;
    private PreparedStatement getUserSalt;
    private PreparedStatement checkUnique;

    {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Драйвер подключен");

            String url = "jdbc:postgresql://127.0.0.1:5432/";
            String name = "postgres";
            String pass = "Test1234";

            connection = DriverManager.getConnection(url, name, pass);
            System.out.println("Соединение успешно установлено\n");

            {
                // Подготовка sql запросов
                loadPersons = connection.prepareStatement("SELECT * FROM persons;");

                addPersonToDB = connection.prepareStatement("INSERT INTO persons VALUES (?, ?, ?, ?, ?, ?)");

                updatePersons = connection.prepareStatement("UPDATE persons SET x = ?, y = ? WHERE username=? AND name=?;");

                executeLogin = connection.prepareStatement("SELECT * FROM users WHERE username=? AND  pass=?;");

                checkRegToken = connection.prepareStatement("SELECT * FROM user_tokens WHERE username=? AND reg_token=?;");

                getEmail = connection.prepareStatement("SELECT * FROM users WHERE username=?;");

                updateRegToken = connection.prepareStatement("UPDATE user_tokens SET reg_token=?, reg_token_time=? WHERE username=?;");

                confirmRegister = connection.prepareStatement("UPDATE users SET email_conf='TRUE' WHERE username=?;");

                removePerson = connection.prepareStatement("DELETE FROM persons WHERE name=? AND username=?;");

                loadPlayerPersons = connection.prepareStatement("SELECT * FROM persons WHERE username=?;");

                checkUnique = connection.prepareStatement("SELECT * FROM users WHERE username=? AND email=?;");

                executeRegister = connection.prepareStatement("INSERT INTO users VALUES (?, ?, ?, 'FALSE', ?);");

                setRegistrationToken = connection.prepareStatement("INSERT INTO user_tokens VALUES (?, ?, ?);");

                getUserSalt = connection.prepareStatement("SELECT salt FROM users WHERE username=?;");

            }

        } catch (Exception e) {
            System.err.println("Не удалось подключиться к БД");
            throw e;
        }
    }

    DataBaseConnection(WorldManager world, Server server) throws Exception {
        this.world = world;
    }

    int loadPersonsFromDB() {
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

                Human person;

                if (side.equals("Spy"))
                    person = new Spy(name, new Location(x, y), time);
                else
                    person = new Merc(name, new Location(x, y), time);

                person.setUser(username);
                world.addNewHuman(person);

                i++;
            }

            return i;
        } catch (Exception e) {
            System.err.printf("Ошибка при загрузке персонажей: %s\n", e.getMessage());
            return -1;
        }
    }

    public void addPersonToDB(String username, Human human) {
        try {
            addPersonToDB.setString(1, human.getName());
            addPersonToDB.setString(2, human.getClass().toString().replace("class Entities.", ""));

            addPersonToDB.setBigDecimal(3, new BigDecimal(human.getLocation().getX()));
            addPersonToDB.setBigDecimal(4, new BigDecimal(human.getLocation().getY()));

            addPersonToDB.setString(5, username);
            addPersonToDB.setTimestamp(6, Timestamp.valueOf(human.getDate()));

            addPersonToDB.executeUpdate();
        } catch (Exception e) {
            System.err.printf("Ошибка при добавлении персонажа в БД: %s\n", e.getMessage());
        }
    }

    void createDB() {
        try {

            connection.prepareStatement(
                    "CREATE TABLE persons (\n" +
                    "    name character varying(16) NOT NULL,\n" +
                    "    side character varying(4) NOT NULL,\n" +
                    "    x numeric(10,2) NOT NULL,\n" +
                    "    y numeric(10,2) NOT NULL,\n" +
                    "    username character varying(20),\n" +
                    "    creation_date timestamp without time zone\n" +
                    ");\n").execute();

            connection.prepareStatement(
                    "CREATE TABLE users (\n" +
                            "    username character varying(20) NOT NULL,\n" +
                            "    email character varying(40),\n" +
                            "    pass text NOT NULL,\n" +
                            "    email_conf boolean DEFAULT false,\n" +
                            "    salt text\n" +
                            ");").execute();

            connection.prepareStatement(
                    "CREATE TABLE user_tokens (\n" +
                    "    username text,\n" +
                    "    reg_token text,\n" +
                    "    reg_token_time timestamp without time zone\n" +
                    ");").execute();

        } catch (Exception e) {
            System.err.printf("Ошибка во время создания бд: %s", e.getMessage());
            System.exit(-1);
        }
    }

    void updatePersons() {
        try {
            for (Human h : world.getPersons().values()) {
                updatePersons.setBigDecimal(1, new BigDecimal(h.getLocation().getX()));
                updatePersons.setBigDecimal(2, new BigDecimal(h.getLocation().getY()));

                updatePersons.setString(3, h.getUser());
                updatePersons.setString(4, h.getName());

                updatePersons.executeUpdate();
            }
        } catch (Exception e) {
            System.err.printf("Ошибка при сохранении персонажей в БД: %s\n", e.getMessage());
        }
    }

    /**
     * Метод авторизации пользователя.
     *
     * @param login
     * @param pass
     * @return
     */
    int executeLogin(String login, String pass) {
        try {
            getUserSalt.setString(1, login);
            ResultSet foundLogin = getUserSalt.executeQuery();

            // Логин не найден
            if (!foundLogin.next()) return 2;

            String salt = foundLogin.getString("salt");
            String hash = getHash(pass, salt);

            executeLogin.setString(1, login);
            executeLogin.setString(2, hash);

            ResultSet result = executeLogin.executeQuery();

            if (result.next()) {
                // Почта подверждена - вход разрешён
                if (result.getBoolean("email_conf"))
                    return 0;
                // Почта не подтвержена
                else
                    return 1;
            } else {
                // Логин и пароль не верный
                return 2;
            }
        } catch (Exception e) {
            System.err.printf("Ошибка авторизации %s: %s\n", login, e.getMessage());
            return -1;
        }
    }

    boolean checkRegToken(Client client, String user, String token) {
        try {

            checkRegToken.setString(1, user);
            checkRegToken.setString(2, token);

            ResultSet result = checkRegToken.executeQuery();

            if (result.next()) {

                LocalDateTime reg_time = LocalDateTime.parse(result.getString("reg_token_time").replace(" ", "T"));
                LocalDateTime now = LocalDateTime.now();

                long time = Duration.between(reg_time, now).get(SECONDS);

                if (time > 60*30) {

                    client.sendMessage(Actions.ALERT, "EXPIRED_REG_TOKEN");

                    getEmail.setString(1, user);
                    ResultSet email = getEmail.executeQuery();
                    email.next();

                    String mail = email.getString("email");
                    String new_token = getToken();

                    LocalDateTime new_time = LocalDateTime.now();

                    updateRegToken.setString(1, new_token);
                    updateRegToken.setTimestamp(2, Timestamp.valueOf(new_time));
                    updateRegToken.setString(3, user);

                    updateRegToken.executeUpdate();

                    new Thread(() -> JavaMail.registration(mail, new_token)).start();

                    return false;
                } else {

                    confirmRegister(user);
                    client.sendMessage(Actions.ALERT, "EMAIL_CONF");

                    return true;
                }
            } else {
                client.sendMessage(Actions.ALERT, "WRONG_TOKEN");
                return false;
            }
        } catch (Exception e) {
            System.err.printf("Ошибка при проверке регистрационного токена: %s\n", e.getMessage());
            return false;
        }
    }

    private void confirmRegister(String username) {
        try {
            confirmRegister.setString(1, username);
            confirmRegister.executeUpdate();
        } catch (Exception e) {
            System.err.printf("Ошибка при подтвеждении регистрации: %s\n", e.getMessage());
        }
    }

    public void removePerson(String name, String username) {
        try {
            removePerson.setString(1, name);
            removePerson.setString(2, username);
            removePerson.executeUpdate();
        } catch (Exception e) {
            System.err.printf("Ошибка при удалении персонажа: %s\n", e.getMessage());
        }
    }

    void sendPersons(Client client) {
        try {

            loadPlayerPersons.setString(1, client.getUserName());
            ResultSet result = loadPlayerPersons.executeQuery();

            while (result.next()) {
                Human person = world.getHuman(result.getString("name"));
                client.addHuman(person);
            }

        } catch (Exception e) {
            System.err.printf("Ошибка при отправке персонажей: %s\n", e.getMessage());
        }
    }

    boolean executeRegister(String login, String password, String email) {
        try {

            String salt = getSalt();
            String hash = getHash(password, salt);

            checkUnique.setString(1, login);
            checkUnique.setString(2, email);

            ResultSet user = checkUnique.executeQuery();

            // Пользователь с такой почтой или логином уже есть
            if (user.next())
                return false;

            executeRegister.setString(1, login);
            executeRegister.setString(2, email);
            executeRegister.setString(3, hash);
            executeRegister.setString(4, salt);

            executeRegister.executeUpdate();

            String reg_token = getToken();

            setRegistrationToken.setString(1, login);
            setRegistrationToken.setString(2, reg_token);
            setRegistrationToken.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            setRegistrationToken.executeUpdate();

            new Thread(() -> JavaMail.registration(email, reg_token)).start();

            return true;
        } catch (Exception e) {
            System.err.printf("Ошибка при регистрации: %s\n", e.getMessage());
            return false;
        }
    }

    static String getToken() {
        try {

            byte[] str = new byte[32];

            SecureRandom.getInstance("SHA1PRNG").nextBytes(str);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            KeySpec spec = new PBEKeySpec("Token".toCharArray(), str, 65536, 128);

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return toHexString(hash);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String getHash(String pass, String salt) {
        try {

            KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt.getBytes(), 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return toHexString(hash);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String getSalt() {
        try {
            byte[] salt = new byte[16];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
            return toHexString(salt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }


}
