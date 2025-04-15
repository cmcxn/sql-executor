package com.sqlexecutor.model;

public class ExecutionResult {
    private String fileName;
    private boolean success;
    private String message;
    private long executionTime;
    private int rowsAffected;

    public ExecutionResult(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public int getRowsAffected() {
        return rowsAffected;
    }

    public void setRowsAffected(int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(fileName).append("\n");
        sb.append("Status: ").append(success ? "Success" : "Failed").append("\n");
        sb.append("Time: ").append(executionTime).append(" ms\n");
        
        if (success) {
            sb.append("Rows affected: ").append(rowsAffected).append("\n");
        } else {
            sb.append("Error: ").append(message).append("\n");
        }
        
        return sb.toString();
    }
}