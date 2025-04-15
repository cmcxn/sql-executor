package com.sqlexecutor.util;

import com.sqlexecutor.model.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private DatabaseConfig config;
    
    public DatabaseManager(DatabaseConfig config) {
        this.config = config;
    }
    
    public Connection getConnection() throws SQLException {
        try {
            // Make sure the PostgreSQL JDBC driver is loaded
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
        
        return DriverManager.getConnection(
                config.getJdbcUrl(), 
                config.getConnectionProperties());
    }
    
    public void testConnection() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Simple query to test connection
            stmt.execute("SELECT 1");
        }
    }
    
    public void setDatabaseConfig(DatabaseConfig config) {
        this.config = config;
    }
}