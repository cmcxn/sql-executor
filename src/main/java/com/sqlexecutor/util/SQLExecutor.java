package com.sqlexecutor.util;

import com.sqlexecutor.model.ExecutionResult;
import com.sqlexecutor.model.SQLFile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLExecutor {
    private DatabaseManager dbManager;
    
    public SQLExecutor(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public ExecutionResult execute(SQLFile sqlFile) {
        ExecutionResult result = new ExecutionResult(sqlFile.getName());
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Execute SQL statements
            String sql = sqlFile.getContent();
            boolean hasResults = stmt.execute(sql);
            int rowsAffected = 0;
            
            if (hasResults) {
                try (ResultSet rs = stmt.getResultSet()) {
                    // Count rows in result set
                    while (rs.next()) {
                        rowsAffected++;
                    }
                }
            } else {
                rowsAffected = stmt.getUpdateCount();
            }
            
            result.setSuccess(true);
            result.setRowsAffected(rowsAffected);
            result.setMessage("SQL executed successfully");
            
        } catch (SQLException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            result.setExecutionTime(endTime - startTime);
        }
        
        return result;
    }
    
    public List<ExecutionResult> executeMultiple(List<SQLFile> sqlFiles) {
        List<ExecutionResult> results = new ArrayList<>();
        
        for (SQLFile sqlFile : sqlFiles) {
            ExecutionResult result = execute(sqlFile);
            results.add(result);
            
            // If an execution fails, stop further execution if needed
            if (!result.isSuccess()) {
                break;
            }
        }
        
        return results;
    }
}