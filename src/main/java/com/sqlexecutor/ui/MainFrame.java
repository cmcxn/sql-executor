package com.sqlexecutor.ui;

import com.sqlexecutor.model.DatabaseConfig;
import com.sqlexecutor.model.ExecutionResult;
import com.sqlexecutor.model.SQLFile;
import com.sqlexecutor.ui.i18n.LanguageManager;
import com.sqlexecutor.util.ConfigManager;
import com.sqlexecutor.util.DatabaseManager;
import com.sqlexecutor.util.FileScanner;
import com.sqlexecutor.util.SQLExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
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
        // Load saved configuration on startup
        this.databaseConfig = ConfigManager.loadConfig();
        initializeUI();

        // Load the last opened folder if available
        String lastFolderPath = ConfigManager.getLastFolderPath();
        if (lastFolderPath != null) {
            File lastFolder = new File(lastFolderPath);
            if (lastFolder.exists() && lastFolder.isDirectory()) {
                SwingUtilities.invokeLater(() -> fileTreePanel.loadFolder(lastFolder));
            }
        }
    }

    private void initializeUI() {
        setTitle(LanguageManager.getString("mainwindow.title"));
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
                // Check for unsaved changes before loading a new file
                if (editorPanel.getCurrentFile() != null) {
                    if (editorPanel.checkUnsavedChanges()) {
                        SQLFile sqlFile = new SQLFile(file);
                        editorPanel.setSqlFile(sqlFile);
                    }
                } else {
                    SQLFile sqlFile = new SQLFile(file);
                    editorPanel.setSqlFile(sqlFile);
                }
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

// Database Config Button
        configButton = new JButton(LanguageManager.getString("mainwindow.configButton"));
        configButton.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
        configButton.setMnemonic(KeyEvent.VK_D); // Alt + D
        configButton.setToolTipText("Configure database settings (Alt+D)");
        configButton.addActionListener(e -> showConfigDialog());
        toolbar.add(configButton);

// Open Folder Button
        JButton openFolderButton = new JButton(LanguageManager.getString("mainwindow.openFolderButton"));
        openFolderButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        openFolderButton.setMnemonic(KeyEvent.VK_O); // Alt + O
        openFolderButton.setToolTipText("Open folder (Alt+O)");
        openFolderButton.addActionListener(e -> openFolder());
        toolbar.add(openFolderButton);

// Execute Selected Button
        executeButton = new JButton(LanguageManager.getString("mainwindow.executeButton"));
        executeButton.setIcon(UIManager.getIcon("InternalFrame.icon"));
        executeButton.setMnemonic(KeyEvent.VK_E); // Alt + E
        executeButton.setToolTipText("Execute selected files (Alt+E)");
        executeButton.addActionListener(e -> executeSelectedFiles());
        toolbar.add(executeButton);

        // Add components to the frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(mainSplitPane, BorderLayout.CENTER);

        // Add window listener for checking unsaved changes on exit
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                if (!editorPanel.checkUnsavedChanges()) {
                    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                } else {
                    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
            }
        });
    }

    private void openFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Set initial directory to last folder if available
        String lastFolderPath = ConfigManager.getLastFolderPath();
        if (lastFolderPath != null) {
            File lastFolder = new File(lastFolderPath);
            if (lastFolder.exists()) {
                fileChooser.setCurrentDirectory(lastFolder);
            }
        }

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();
            fileTreePanel.loadFolder(folder);

            // Save the selected folder path to configuration
            ConfigManager.saveLastFolderPath(folder.getAbsolutePath());
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
            // Save the configuration whenever it's updated
            ConfigManager.saveConfig(this.databaseConfig);
        }
    }
}