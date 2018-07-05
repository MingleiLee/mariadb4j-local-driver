package com.jeedsoft.marialocal;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeedsoft.marialocal.util.SystemUtil;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * MariaDB4j instance manager
 * 
 * 1. When apply connection of URL jdbc:marialocal:/opt/data/school for the first time, A new MariaDB
 * instance will be started with data directory /opt/data.
 * 
 * 2. When apply another connection of URL jdbc:marialocal:/opt/data/church, The previous MariaDB instance
 * will be reused since the data directories are identical (also /opt/data). 
 * 
 * 3. When apply another connection of URL jdbc:marialocal:/opt/storage/road for the first time, A new
 * MariaDB instance will be started with data directory /opt/storage.
 */
public class MariaLocalManager
{
    private static final Logger logger = LoggerFactory.getLogger(MariaLocalManager.class);
    
    private static final Map<String, DBConfigurationBuilder> configMap = new HashMap<>();

    private static final Map<String, DB> instanceMap = new HashMap<>();

    private static final Map<String, Set<String>> databaseMap = new HashMap<>();

    private static final Map<String, String> globalArgs = new HashMap<>();
    
    static
    {
        globalArgs.put("character-set-server", "utf8mb4");
        globalArgs.put("collation-server", "utf8mb4_general_ci");
        if (SystemUtil.isWindows()) {
            globalArgs.put("lower_case_table_names", "2");
        }
    }
    
    /**
     * Convert local-style URL (jdbc:marialocal:<file-path>) to remote-style
     * URL (jdbc:mysql://<server>:<port>/<database>). The MariaDB instance
     * will be automatically started when call this method.
     */
    static String toRemoteUrl(String url) throws SQLException
    {
        Matcher matcher = MariaLocalDriver.urlPattern.matcher(url);
        if (!matcher.matches()) {
            return null;
        }
        try {
            File dbFile = new File(matcher.group(1));
            File instanceDir = dbFile.getParentFile();
            String instancePath = instanceDir.getCanonicalPath();
            String dbName = dbFile.getName();
            if (!configMap.containsKey(instancePath)) {
                synchronized (configMap) {
                    if (!configMap.containsKey(instancePath)) { // double check
                        logger.info("Create local MariaDB instance: {}", instancePath);
                        DBConfigurationBuilder config = config(0, instancePath);
                        DB db = DB.newEmbeddedDB(config.build());
                        db.start();
                        configMap.put(instancePath, config);
                        instanceMap.put(instancePath, db);
                        databaseMap.put(instancePath, new HashSet<>());
                    }
                }
            }
            Set<String> set = databaseMap.get(instancePath);
            if (!set.contains(dbName)) {
                synchronized (set) {
                    if (!set.contains(dbName)) { // double check
                        logger.info("Create local MariaDB database: {}", dbFile.getCanonicalPath());
                        instanceMap.get(instancePath).createDB(dbName);;
                        set.add(dbName);
                    }
                }
            }
            return configMap.get(instancePath).getURL(dbName);
        }
        catch (Exception e) {
            throw new SQLException("");
        }
    }
    
    /**
     * Shutdown all the MariaDB instances. This method should be invoked when the application is exiting.
     */
    public static void shutdown()
    {
        for (Entry<String, DB> entry: instanceMap.entrySet()) {
            try {
                entry.getValue().stop();
            }
            catch (ManagedProcessException e) {
                logger.error("Failed to stop MariaDB4j instance: " + entry.getKey(), e);
            }
        }
    }
    
    public static String getGlobalArg(String key)
    {
        return globalArgs.get(key);
    }
    
    public static void setGlobalArg(String key, String value)
    {
        globalArgs.put(key, value);
    }

    public static String removeGlobalArg(String key)
    {
        return globalArgs.remove(key);
    }
    
    static DBConfigurationBuilder config(int port, String dataDir)
    {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0);
        config.setDataDir(dataDir);
        for (Map.Entry<String, String> entry: globalArgs.entrySet()) {
            config.addArg("--" + entry.getKey() + "=" + entry.getValue());
        }
        return config;
    }
}
