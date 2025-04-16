package com.sqlexecutor.ui;

import com.sqlexecutor.model.DatabaseConfig;
import com.sqlexecutor.model.ExecutionResult;
import com.sqlexecutor.model.SQLFile;
import com.sqlexecutor.util.DatabaseManager;
import com.sqlexecutor.util.FileScanner;
import com.sqlexecutor.util.SQLExecutor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private DatabaseConfig databaseConfig;
    private FileTreePanel fileTreePanel;
    private SQLEditorPanel editorPanel;
    private ExecutionPanel executionPanel;
    private JButton executeButton;
    private JButton configButton;
    
    public MainFrame() {
        this.databaseConfig = new DatabaseConfig();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("SQL Executor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Create main split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(300);
        
        // Create file tree panel (left side)
        fileTreePanel = new FileTreePanel();
        fileTreePanel.addFileSelectionListener(file -> {
            if (file != null && file.exists() && !file.isDirectory()) {
                SQLFile sqlFile = new SQLFile(file);
                editorPanel.setSqlFile(sqlFile);
            }
        });
        
        // Create editor panel (right side)
        editorPanel = new SQLEditorPanel();
        
        // Create execution panel
        executionPanel = new ExecutionPanel();
        
        // Create a vertical split for editor and execution panel
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setTopComponent(editorPanel);
        rightSplitPane.setBottomComponent(executionPanel);
        rightSplitPane.setDividerLocation(500);
        
        mainSplitPane.setLeftComponent(new JScrollPane(fileTreePanel));
        mainSplitPane.setRightComponent(rightSplitPane);
        
        // Create toolbar with buttons
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        configButton = new JButton("Database Config");
        configButton.addActionListener(e -> showConfigDialog());

        JButton openFolderButton = new JButton("Open Folder");
        openFolderButton.addActionListener(e -> openFolder());

        executeButton = new JButton("Execute Selected");

        executeButton.addActionListener(e -> executeSelectedFiles());

        toolbar.add(configButton);
        toolbar.add(openFolderButton);
        toolbar.add(executeButton);

        // Add components to the frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(mainSplitPane, BorderLayout.CENTER);
    }
    
    private void openFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();
            fileTreePanel.loadFolder(folder);
        }
    }
    
    private void executeSelectedFiles() {
        List<File> selectedFiles = fileTreePanel.getSelectedFiles();
        
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No files selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            DatabaseManager dbManager = new DatabaseManager(databaseConfig);
            SQLExecutor executor = new SQLExecutor(dbManager);
            
            executionPanel.clear();
            executionPanel.appendText("Starting SQL execution...\n");
            
            List<SQLFile> sqlFiles = selectedFiles.stream()
                    .map(SQLFile::new)
                    .collect(Collectors.toList());
            
            for (SQLFile sqlFile : sqlFiles) {
                ExecutionResult result = executor.execute(sqlFile);
                executionPanel.appendText("\n" + result.toString() + "\n");
            }
            
            executionPanel.appendText("\nExecution completed.");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            executionPanel.appendText("\nError: " + e.getMessage());
        }
    }
    
    private void showConfigDialog() {
        ConfigDialog dialog = new ConfigDialog(this, databaseConfig);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            this.databaseConfig = dialog.getDatabaseConfig();
        }
    }
}