package com.sqlexecutor.ui;

import com.sqlexecutor.model.SQLFile;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;

public class SQLEditorPanel extends JPanel {
    private RSyntaxTextArea textArea;
    private JLabel fileNameLabel;
    private SQLFile currentFile;
    private boolean modified = false;
    private JButton saveButton;

    public SQLEditorPanel() {
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Create file name label
        fileNameLabel = new JLabel("No file selected");
        fileNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fileNameLabel.setFont(fileNameLabel.getFont().deriveFont(Font.BOLD));

        // Create RSyntaxTextArea for SQL syntax highlighting
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setTabSize(2);

        // Add SQL-specific enhancements
        textArea.setMarkOccurrences(true);
        textArea.setMarkOccurrencesDelay(500);
        textArea.setPaintTabLines(true);
        textArea.setAutoIndentEnabled(true);

        // Add document listener to track modifications
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
            }
        });

        // Allow editing
        textArea.setEditable(true);

        // Add keyboard shortcuts for common operations
        addEditorShortcuts();

        // Create a RTextScrollPane for the editor (special scrollpane for RSyntaxTextArea)
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);

        // Build a simple toolbar with buttons
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Add Save button
        saveButton = new JButton("Save");
        saveButton.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveCurrentFile());
        toolbar.add(saveButton);

        // Add separator
        toolbar.addSeparator();

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

        // Add Ctrl+S (Save)
        KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        Action saveAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isModified() && currentFile != null) {
                    saveCurrentFile();
                }
            }
        };
        inputMap.put(saveKeyStroke, "save");
        actionMap.put("save", saveAction);

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
        if (checkUnsavedChanges()) {
            this.currentFile = sqlFile;

            if (sqlFile != null) {
                fileNameLabel.setText(sqlFile.getAbsolutePath());
                textArea.setText(sqlFile.getContent());
                textArea.setCaretPosition(0);
                setModified(false);
            } else {
                fileNameLabel.setText("No file selected");
                textArea.setText("");
                setModified(false);
            }
        }
    }

    /**
     * Checks if there are unsaved changes and prompts the user if needed.
     * @return true if it's safe to continue (user saved or chose to discard changes),
     *         false if the operation should be cancelled
     */
    public boolean checkUnsavedChanges() {
        if (isModified() && currentFile != null) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "The file '" + currentFile.getName() + "' has been modified. Save changes?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                return saveCurrentFile();
            } else return choice != JOptionPane.CANCEL_OPTION;
        }
        return true;
    }

    /**
     * Saves the current file
     * @return true if save was successful, false otherwise
     */
    public boolean saveCurrentFile() {
        if (currentFile == null) return false;

        try (FileWriter writer = new FileWriter(currentFile.getFile())) {
            String content = textArea.getText();
            writer.write(content);

            // Update content in the SQLFile object
            currentFile.setContent(content);

            setModified(false);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error saving file: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    public void setModified(boolean modified) {
        this.modified = modified;
        saveButton.setEnabled(modified);

        // Update UI to show modified status (optional)
        if (currentFile != null) {
            String title = currentFile.getAbsolutePath();
            if (modified) {
                title += " *";
            }
            fileNameLabel.setText(title);
        }
    }

    public boolean isModified() {
        return modified;
    }

    public SQLFile getCurrentFile() {
        return currentFile;
    }

    // Getter for the text area - can be useful if you need to access it from outside
    public RSyntaxTextArea getTextArea() {
        return textArea;
    }
}