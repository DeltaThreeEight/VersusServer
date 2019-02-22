package IOStuff;

import Entities.*;
import World.Locations;
import com.google.gson.*;

import java.lang.reflect.Type;

public class MyDeserialize implements JsonDeserializer<Creature>  {

    public Creature deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        JsonObject locObject = object.get("loc").getAsJsonObject();

        String locName = locObject.get("name").getAsString();
        Double x = locObject.get("x").getAsDouble();
        Double y = locObject.get("y").getAsDouble();
        Boolean building = locObject.get("isBuilding").getAsBoolean();

        Locations loc = new Locations(x,y,locName,building);

        String name = object.get("name").getAsString();
        String jtype = object.get("type").getAsString();
        if (jtype.equals("Human")) return new Human(name, loc);
        if (jtype.equals("Shoggot")) return new Shoggot(name, loc);

        Animals atype = Animals.valueOf(object.get("kindOfAnimal").getAsString());
        if (jtype.equals("Animal")) return new Animal(name, atype, loc);
        throw new JsonParseException("Не удалось распознать тип");
    }
}
