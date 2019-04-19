package xyz.rc24.bot.datamover;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.rc24.bot.database.Database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class BirthdayManager
{
    private Database db;
    private Map<Long, String> map;

    private Method insertMethod;

    BirthdayManager(DataMover main) throws NoSuchMethodException
    {
        this.db = main.getDatabase();
        this.map = new HashMap<>();

        this.insertMethod = main.getDatabase().getClass().getDeclaredMethod("doInsert", String.class, Object[].class);
        this.insertMethod.setAccessible(true);
    }

    void constructMap(JsonArray array)
    {
        // Get the birthdays object
        JsonObject obj = array.get(0).getAsJsonObject().get("birthdays").getAsJsonObject();

        // Loop on them
        for(Map.Entry<String, JsonElement> entry : obj.entrySet())
        {
            long id = Long.parseLong(entry.getKey());
            String[] split = entry.getValue().getAsString().split("/"); // MM/DD -> DD/MM

            map.put(id, split[1] + "/" + split[0]);
        }

        array.remove(obj); // Remove the birthdays object
    }

    void saveToDatabase() throws InvocationTargetException, IllegalAccessException
    {
        for(Map.Entry<Long, String> entry : map.entrySet())
        {
            insertMethod.invoke(db, "INSERT INTO birthdays VALUES(?, ?)",
                    new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}
