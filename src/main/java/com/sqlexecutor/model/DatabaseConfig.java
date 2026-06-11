package com.sqlexecutor.model;

import java.util.Properties;

public class DatabaseConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String databaseName;
    private String schema;

    public DatabaseConfig() {
        // Default values
        this.host = "localhost";
        this.port = 5432;
        this.username = "postgres";
        this.password = "";
        this.databaseName = "postgres";
        this.schema = "";
    }

    public DatabaseConfig(String host, int port, String username, String password, String databaseName) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.schema = "";
    }

    public DatabaseConfig(String host, int port, String username, String password, String databaseName, String schema) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.schema = schema != null ? schema : "";
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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema != null ? schema : "";
    }

    public String getJdbcUrl() {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
        if (schema != null && !schema.trim().isEmpty()) {
            url += "?currentSchema=" + schema.trim();
        }
        return url;
    }

    public Properties getConnectionProperties() {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        return props;
    }
}