package com.sqlexecutor.util;

import com.sqlexecutor.model.DatabaseConfig;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILENAME = "sqlexecutor.properties";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".sqlexecutor";
    private static final String CONFIG_PATH = CONFIG_DIR + File.separator + CONFIG_FILENAME;

    static {
        // Ensure the config directory exists
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public static void saveConfig(DatabaseConfig config, String folderPath) {
        Properties props = new Properties();
        props.setProperty("host", config.getHost());
        props.setProperty("port", String.valueOf(config.getPort()));
        props.setProperty("username", config.getUsername());
        props.setProperty("password", config.getPassword());
        props.setProperty("databaseName", config.getDatabaseName());

        // Save folder path if not null
        if (folderPath != null) {
            props.setProperty("lastFolderPath", folderPath);
        }

        try (OutputStream out = new FileOutputStream(CONFIG_PATH)) {
            props.store(out, "SQL Executor Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }

    // Overloaded method for backward compatibility
    public static void saveConfig(DatabaseConfig config) {
        String lastFolderPath = getLastFolderPath();
        saveConfig(config, lastFolderPath);
    }

    public static void saveLastFolderPath(String folderPath) {
        DatabaseConfig config = loadConfig();
        saveConfig(config, folderPath);
    }

    public static DatabaseConfig loadConfig() {
        File configFile = new File(CONFIG_PATH);
        if (!configFile.exists()) {
            return new DatabaseConfig(); // Return default config if file doesn't exist
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configFile)) {
            props.load(in);

            String host = props.getProperty("host", "localhost");
            int port = Integer.parseInt(props.getProperty("port", "5432"));
            String username = props.getProperty("username", "postgres");
            String password = props.getProperty("password", "");
            String databaseName = props.getProperty("databaseName", "postgres");

            return new DatabaseConfig(host, port, username, password, databaseName);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            return new DatabaseConfig(); // Return default config on error
        }
    }

    public static String getLastFolderPath() {
        File configFile = new File(CONFIG_PATH);
        if (!configFile.exists()) {
            return null;
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configFile)) {
            props.load(in);
            return props.getProperty("lastFolderPath");
        } catch (IOException e) {
            System.err.println("Failed to load last folder path: " + e.getMessage());
            return null;
        }
    }
}