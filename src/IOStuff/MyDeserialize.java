package IOStuff;

import Entities.Human;
import Entities.Merc;
import Entities.Spy;
import World.Location;
import com.google.gson.*;

import java.lang.reflect.Type;

public class MyDeserialize implements JsonSerializer<Human>, JsonDeserializer<Human> {

    public Human deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        JsonObject locObject = object.get("loc").getAsJsonObject();

        Double x = locObject.get("x").getAsDouble();
        Double y = locObject.get("y").getAsDouble();

        Location loc = new Location(x,y);

        String name = object.get("name").getAsString();
        String jtype = object.get("side").getAsString();
        if (jtype.equals("Spy")) return new Spy(name, loc);
        if (jtype.equals("Merc")) return new Merc(name, loc);

        throw new JsonParseException("Не удалось распознать тип");
    }

    public JsonElement serialize(Human src, Type type,
                                 JsonSerializationContext context) {
        JsonObject human = new JsonObject();
        JsonObject loc = new JsonObject();

        loc.addProperty("x", src.getLocation().getX());
        loc.addProperty("y", src.getLocation().getY());

        human.addProperty("side", src.getClass().toString().replace("class Entities.", ""));
        human.addProperty("name", src.getName());
        human.add("loc", loc);
        return human;
    }
}
