package com.sqlexecutor.ui;

import com.sqlexecutor.ui.i18n.LanguageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ExecutionPanel extends JPanel {
    private JTextArea textArea;
    private JScrollPane scrollPane;
    
    public ExecutionPanel() {
        setLayout(new BorderLayout());
        initializeUI();
    }
    
    private void initializeUI() {
        // Create a label for the panel
        JLabel titleLabel = new JLabel("Execution Results");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        
        // Create text area for results
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        scrollPane = new JScrollPane(textArea);
        
        // Create toolbar with clear button
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        //clear_button
        JButton clearButton = new JButton(LanguageManager.getString("mainwindow.clear_button"));
        clearButton.setIcon(UIManager.getIcon("html.missingImage"));
        clearButton.addActionListener(e -> clear());
        clearButton.setMnemonic(KeyEvent.VK_C); // Alt + C
        toolbar.add(clearButton);
        
        // Add components to panel
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);
    }
    
    public void appendText(String text) {
        textArea.append(text);
        // Scroll to bottom
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    
    public void clear() {
        textArea.setText("");
    }
}