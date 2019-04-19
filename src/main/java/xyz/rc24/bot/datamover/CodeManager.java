package xyz.rc24.bot.datamover;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.rc24.bot.core.entities.CodeType;
import xyz.rc24.bot.database.Database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class CodeManager
{
    private Database db;
    private Gson gson;
    private Map<Long, Map<CodeType, Map<String, String>>> map;

    private Method insertMethod;

    CodeManager(DataMover main) throws NoSuchMethodException
    {
        this.db = main.getDatabase();
        this.gson = main.getGson();
        this.map = new HashMap<>();

        this.insertMethod = main.getDatabase().getClass().getDeclaredMethod("doInsert", String.class, Object[].class);
        this.insertMethod.setAccessible(true);
    }

    void constructMap(JsonArray array)
    {
        // Get the codes object
        JsonObject obj = array.get(0).getAsJsonObject();

        // Loop on them
        for(Map.Entry<String, JsonElement> entry : obj.entrySet())
        {
            String key = entry.getKey(); // ID:TYPE
            if(!(key.matches("(\\d+):([A-Z_]+)")))
                continue;

            String[] split = key.split(":");
            long id = Long.parseLong(split[0]);
            CodeType type = remapLegacyCodeType(split[1]);

            Map<CodeType, Map<String, String>> userMap = map.getOrDefault(id, new HashMap<>());
            Map<String, String> codesForTypeMap = userMap.getOrDefault(type, new HashMap<>());

            for(Map.Entry<String, JsonElement> code : entry.getValue().getAsJsonObject().entrySet())
                codesForTypeMap.put(code.getKey(), code.getValue().getAsString());

            userMap.put(type, codesForTypeMap);
            map.put(id, userMap);
        }

        array.remove(obj); // Remove the codes object
    }

    void saveToDatabase() throws InvocationTargetException, IllegalAccessException
    {
        for(Map.Entry<Long, Map<CodeType, Map<String, String>>> entry : map.entrySet())
        {
            for(Map.Entry<CodeType, Map<String, String>> codesForTypeMap : entry.getValue().entrySet())
            {
                CodeType type = codesForTypeMap.getKey();
                long id = entry.getKey();
                String json = gson.toJson(codesForTypeMap.getValue());

                insertMethod.invoke(db, "INSERT INTO codes (user_id, " + type.getColumn() + ") " + "VALUES(?, ?)" +
                        "ON DUPLICATE KEY UPDATE " + type.getColumn() + " = ?", new Object[]{id, json, json});
            }
        }
    }

    private CodeType remapLegacyCodeType(String legacy)
    {
        switch(legacy)
        {
            case "THREE_DS":
                return CodeType.THREEDS;
            case "GAMES":
                return CodeType.GAME;
            default:
                return CodeType.valueOf(legacy);
        }
    }
}
