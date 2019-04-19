package xyz.rc24.bot.datamover;

import co.aikar.idb.DB;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import xyz.rc24.bot.database.Database;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataMover
{
    private boolean ssl, autoReconnect, verifyCert;
    private String user, password, name, host;

    private Database db;
    private Gson gson = new Gson();

    private BirthdayManager birthdayManager;
    private CodeManager codeManager;
    private GuildSettingsManager guildSettingsManager;

    public static void main(String[] args) throws Exception
    {
        if(args.length == 0)
            throw new RuntimeException("No RDB dump json file specified!");

        File json = new File(args[0]);
        if(!(json.exists()))
            throw new IllegalArgumentException("The specified RDB dump json file was not found!");

        new DataMover().run(json);
    }

    private void run(File json) throws Exception
    {
        System.out.println("Loading config...");
        File config = new File("config.txt");
        List<String> lines = Files.readAllLines(config.toPath());

        initConfig(lines);

        System.out.println("Connecting to database...");
        initDatabase();

        System.out.println("Initializing managers...");
        initManagers();

        System.out.println("-----------------------------------------------------------------");
        System.out.println("Parsing JSON file...");

        JsonArray array = gson.fromJson(new FileReader(json), JsonArray.class);
        System.out.println("-----------------------------------------------------------------");

        System.out.println("Starting code dumping...");
        codeManager.constructMap(array);
        System.out.println("Successfully dumped codes.");

        System.out.println("-----------------------------------------------------------------");

        System.out.println("Starting server settings dumping...");
        guildSettingsManager.constructMap(array);
        System.out.println("Successfully dumped server settings.");

        System.out.println("-----------------------------------------------------------------");

        System.out.println("Starting birthday dumping...");
        birthdayManager.constructMap(array);
        System.out.println("Successfully dumped birthdays.");

        System.out.println("-----------------------------------------------------------------");

        System.out.println("Saving codes to database...");
        codeManager.saveToDatabase();
        System.out.println("Successfully saved codes to database.");

        System.out.println("-----------------------------------------------------------------");

        System.out.println("Saving birthdays to database...");
        birthdayManager.saveToDatabase();
        System.out.println("Successfully saved birthdays to database.");

        System.out.println("-----------------------------------------------------------------");

        System.out.println("Saving guild settings to database...");
        guildSettingsManager.saveToDatabase();
        System.out.println("Successfully saved guild settings to database.");

        System.out.println("-----------------------------------------------------------------");

        System.out.println("Successfully finished data moving. Exiting.");
        DB.close();
        System.exit(0);
    }

    /**
     * 0 - user
     * 1 - password
     * 2 - name
     * 3 - host
     * 4 - useSSL
     * 5 - autoReconnect
     * 6 - verifyServerCertificate
     *
     * @param lines Config lines
     */
    private void initConfig(List<String> lines)
    {
        this.user = lines.get(0);
        this.password = lines.get(1);
        this.name = lines.get(2);
        this.host = lines.get(3);
        this.ssl = Boolean.parseBoolean(lines.get(4));
        this.autoReconnect = Boolean.parseBoolean(lines.get(5));
        this.verifyCert = Boolean.parseBoolean(lines.get(6));
    }

    private void initDatabase()
    {
        DatabaseOptions options = DatabaseOptions.builder()
                .mysql(user, password, name, host)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .dataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource")
                .build();

        Map<String, Object> props = new HashMap<String, Object>()
        {{
            put("useSSL", ssl);
            put("verifyServerCertificate", verifyCert);
            put("autoReconnect", autoReconnect);
            put("serverTimezone", "CST");
        }};

        co.aikar.idb.Database db = PooledDatabaseOptions.builder()
                .dataSourceProperties(props)
                .options(options)
                .createHikariDatabase();

        DB.setGlobalDatabase(db);

        this.db = new Database();
    }

    private void initManagers() throws NoSuchMethodException
    {
        this.birthdayManager = new BirthdayManager(this);
        this.codeManager = new CodeManager(this);
        this.guildSettingsManager = new GuildSettingsManager(this);
    }

    Database getDatabase()
    {
        return db;
    }

    Gson getGson()
    {
        return gson;
    }
}
