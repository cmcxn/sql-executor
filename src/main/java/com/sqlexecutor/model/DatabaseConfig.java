package com.sqlexecutor.model;

import java.util.Properties;

public class DatabaseConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String databaseName;

    public DatabaseConfig() {
        // Default values
        this.host = "localhost";
        this.port = 5432;
        this.username = "postgres";
        this.password = "";
        this.databaseName = "postgres";
    }

    public DatabaseConfig(String host, int port, String username, String password, String databaseName) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    public Properties getConnectionProperties() {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        return props;
    }
}