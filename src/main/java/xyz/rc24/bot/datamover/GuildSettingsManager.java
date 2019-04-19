package xyz.rc24.bot.datamover;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.rc24.bot.core.entities.CodeType;
import xyz.rc24.bot.core.entities.GuildSettings;
import xyz.rc24.bot.core.entities.impl.GuildSettingsImpl;
import xyz.rc24.bot.database.Database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class GuildSettingsManager
{
    private Database db;
    private Map<Long, GuildSettings> map;

    private Method insertMethod;

    GuildSettingsManager(DataMover main) throws NoSuchMethodException
    {
        this.db = main.getDatabase();
        this.map = new HashMap<>();

        this.insertMethod = main.getDatabase().getClass().getDeclaredMethod("doInsert", String.class, Object[].class);
        this.insertMethod.setAccessible(true);
    }

    void constructMap(JsonArray array)
    {
        // Get the guild settings object
        JsonObject obj = array.get(0).getAsJsonObject();

        // Loop on them
        for(Map.Entry<String, JsonElement> entry : obj.entrySet())
        {
            long id = Long.parseLong(entry.getKey());
            GuildSettingsImpl gs = new GuildSettingsImpl(CodeType.WII, 0L, id, 0L, 0L, null);

            for(Map.Entry<String, JsonElement> setting : entry.getValue().getAsJsonObject().entrySet())
            {
                switch(setting.getKey())
                {
                    case "MOD":
                    {
                        gs.setModlogId(setting.getValue().getAsLong());
                        break;
                    }
                    case "SERVER":
                    {
                        gs.setServerlogId(setting.getValue().getAsLong());
                        break;
                    }
                    case "addType":
                    {
                        gs.setDefaultAddType(CodeType.valueOf(setting.getValue().getAsString()));
                        break;
                    }
                }
            }

            map.put(id, gs);
        }

        array.remove(obj); // Remove the guild settings object
    }

    void saveToDatabase() throws InvocationTargetException, IllegalAccessException
    {
        for(Map.Entry<Long, GuildSettings> entry : map.entrySet())
        {
            GuildSettings gs = entry.getValue();
            insertMethod.invoke(db, "INSERT INTO settings VALUES (?, ?, ?, ?, ?, ?)", new Object[]{
                    gs.getGuildId(),
                    gs.getModlogChannelId(),
                    gs.getServerlogChannelId(),
                    gs.getBirthdaysChannelId(),
                    gs.getDefaultAddType().getId(),
                    null
            });
        }
    }
}
