package com.sqlexecutor.ui;

import com.sqlexecutor.model.SQLFile;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class SQLEditorPanel extends JPanel {
    private JTextArea textArea;
    private JLabel fileNameLabel;
    private SQLFile currentFile;

    public SQLEditorPanel() {
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Create file name label
        fileNameLabel = new JLabel("No file selected");
        fileNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fileNameLabel.setFont(fileNameLabel.getFont().deriveFont(Font.BOLD));

        // Create text area with monospaced font for code display
        textArea = new JTextArea(20, 60);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setTabSize(2);

        // Add line wrapping
        textArea.setLineWrap(false);
        textArea.setWrapStyleWord(true);

        // Initially set as read-only
        textArea.setEditable(false);

        // Add keyboard shortcuts for common operations
        addEditorShortcuts();

        // Create a scrollpane for the editor
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Build a simple toolbar with buttons
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> textArea.copy());

        JButton wrapButton = new JButton("Toggle Wrap");
        wrapButton.addActionListener(e -> textArea.setLineWrap(!textArea.getLineWrap()));

        toolbar.add(copyButton);
        toolbar.add(wrapButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(fileNameLabel, BorderLayout.CENTER);
        topPanel.add(toolbar, BorderLayout.EAST);

        // Add components to panel
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addEditorShortcuts() {
        InputMap inputMap = textArea.getInputMap();
        ActionMap actionMap = textArea.getActionMap();

        // Add Ctrl+A (Select All)
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(keyStroke, DefaultEditorKit.selectAllAction);

        // Add Ctrl+C (Copy)
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        inputMap.put(keyStroke, DefaultEditorKit.copyAction);

        // Add Ctrl+F (Find) - Basic implementation
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        Action findAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = JOptionPane.showInputDialog(
                        SQLEditorPanel.this,
                        "Find:",
                        "Find Text",
                        JOptionPane.PLAIN_MESSAGE);

                if (searchTerm != null && !searchTerm.isEmpty()) {
                    String text = textArea.getText();
                    int index = text.indexOf(searchTerm, textArea.getCaretPosition());

                    if (index >= 0) {
                        textArea.setCaretPosition(index);
                        textArea.select(index, index + searchTerm.length());
                        textArea.requestFocusInWindow();
                    } else {
                        // Start from beginning if not found
                        index = text.indexOf(searchTerm, 0);
                        if (index >= 0) {
                            textArea.setCaretPosition(index);
                            textArea.select(index, index + searchTerm.length());
                            textArea.requestFocusInWindow();
                        } else {
                            JOptionPane.showMessageDialog(
                                    SQLEditorPanel.this,
                                    "Text not found",
                                    "Find",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        };
        inputMap.put(keyStroke, "find");
        actionMap.put("find", findAction);
    }

    public void setSqlFile(SQLFile sqlFile) {
        this.currentFile = sqlFile;

        if (sqlFile != null) {
            fileNameLabel.setText(sqlFile.getAbsolutePath());
            textArea.setText(sqlFile.getContent());
            textArea.setCaretPosition(0);
        } else {
            fileNameLabel.setText("No file selected");
            textArea.setText("");
        }
    }

    public SQLFile getCurrentFile() {
        return currentFile;
    }
}