package Server;

import Entities.Human;
import Entities.Merc;
import Entities.Spy;
import World.Location;
import World.WorldManager;
import com.lambdaworks.crypto.SCryptUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

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
            statement.executeUpdate("INSERT INTO users VALUES ('" + login + "', '" +mail+ "', '"+hash+"', 'FALSE', '"+"sdadda"+"');");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
