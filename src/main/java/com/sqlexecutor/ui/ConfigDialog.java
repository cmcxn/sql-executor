package com.sqlexecutor.ui;

import com.sqlexecutor.model.DatabaseConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigDialog extends JDialog {
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField databaseNameField;
    private boolean confirmed = false;
    private DatabaseConfig databaseConfig;
    
    public ConfigDialog(Frame owner, DatabaseConfig config) {
        super(owner, "Database Configuration", true);
        this.databaseConfig = new DatabaseConfig(
                config.getHost(),
                config.getPort(),
                config.getUsername(),
                config.getPassword(),
                config.getDatabaseName()
        );
        
        initializeUI();
        pack();
        setLocationRelativeTo(owner);
    }
    
    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Host field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Host:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        hostField = new JTextField(databaseConfig.getHost(), 20);
        panel.add(hostField, gbc);
        
        // Port field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Port:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        portField = new JTextField(String.valueOf(databaseConfig.getPort()), 20);
        panel.add(portField, gbc);
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(databaseConfig.getUsername(), 20);
        panel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(databaseConfig.getPassword(), 20);
        panel.add(passwordField, gbc);
        
        // Database name field
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Database:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        databaseNameField = new JTextField(databaseConfig.getDatabaseName(), 20);
        panel.add(databaseNameField, gbc);
        
        // Test connection button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        JButton testButton = new JButton("Test Connection");
        testButton.addActionListener(e -> testConnection());
        panel.add(testButton, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            updateDatabaseConfig();
            confirmed = true;
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to dialog
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void updateDatabaseConfig() {
        databaseConfig.setHost(hostField.getText());
        try {
            databaseConfig.setPort(Integer.parseInt(portField.getText()));
        } catch (NumberFormatException e) {
            databaseConfig.setPort(5432); // Default PostgreSQL port
        }
        databaseConfig.setUsername(usernameField.getText());
        databaseConfig.setPassword(new String(passwordField.getPassword()));
        databaseConfig.setDatabaseName(databaseNameField.getText());
    }
    
    private void testConnection() {
        updateDatabaseConfig();
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String errorMessage;
            
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    com.sqlexecutor.util.DatabaseManager manager = 
                            new com.sqlexecutor.util.DatabaseManager(databaseConfig);
                    manager.testConnection();
                    return true;
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(ConfigDialog.this,
                                "Connection successful!",
                                "Test Connection",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ConfigDialog.this,
                                "Connection failed: " + errorMessage,
                                "Test Connection",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ConfigDialog.this,
                            "Error: " + e.getMessage(),
                            "Test Connection",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }
}