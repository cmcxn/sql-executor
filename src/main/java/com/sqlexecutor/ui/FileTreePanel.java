package com.sqlexecutor.ui;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class FileTreePanel extends JPanel {
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private File currentFolder;
    private List<Consumer<File>> fileSelectionListeners = new ArrayList<>();

    public FileTreePanel() {
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        rootNode = new DefaultMutableTreeNode("SQL Files");
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel) {
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded, 
                                           boolean leaf, int row, boolean hasFocus) {
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
                    if (userObject instanceof FileNode) {
                        return ((FileNode)userObject).getFile().getName();
                    }
                }
                return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
            }
        };
        
        fileTree.setCellRenderer(new CheckboxTreeCellRenderer());
        fileTree.setCellEditor(new CheckboxTreeCellEditor(fileTree));
        fileTree.setEditable(true);
        
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        fileTree.getLastSelectedPathComponent();
                
                if (node == null) return;
                
                Object userObject = node.getUserObject();
                if (userObject instanceof FileNode) {
                    FileNode fileNode = (FileNode)userObject;
                    File file = fileNode.getFile();
                    
                    // Notify listeners only for file selections (not directories)
                    if (!file.isDirectory()) {
                        for (Consumer<File> listener : fileSelectionListeners) {
                            listener.accept(file);
                        }
                    }
                }
            }
        });
        
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = fileTree.getRowForLocation(e.getX(), e.getY());
                if(row < 0) return;
                
                Rectangle rowBounds = fileTree.getRowBounds(row);
                // Calculate checkbox bounds to detect click on checkbox area
                int checkboxWidth = 20;
                if (e.getX() < (rowBounds.x + checkboxWidth)) {
                    TreePath path = fileTree.getPathForRow(row);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                    if (node.getUserObject() instanceof FileNode) {
                        FileNode fileNode = (FileNode)node.getUserObject();
                        fileNode.setSelected(!fileNode.isSelected());
                        
                        // If it's a directory, toggle all children
                        if (fileNode.getFile().isDirectory()) {
                            toggleChildren(node, fileNode.isSelected());
                        }
                        
                        // Repaint to show the updated checkbox state
                        fileTree.repaint();
                    }
                }
            }
        });
        
        add(new JScrollPane(fileTree), BorderLayout.CENTER);
    }
    
    public void loadFolder(File folder) {
        this.currentFolder = folder;
        rootNode.removeAllChildren();
        
        DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(
                new FileNode(folder, false, true));
        rootNode.add(folderNode);
        
        loadFilesIntoNode(folder, folderNode);
        
        // Expand the tree to show the structure
        treeModel.reload();
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
    }
    
    private void loadFilesIntoNode(File folder, DefaultMutableTreeNode node) {
        File[] files = folder.listFiles(file -> 
            file.isDirectory() || file.getName().toLowerCase().endsWith(".sql"));
        
        if (files == null) return;
        
        // Sort files: directories first, then alphabetically
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            }
        });
        
        for (File file : files) {
            FileNode fileNode = new FileNode(file, false, file.isDirectory());
            DefaultMutableTreeNode fileTreeNode = new DefaultMutableTreeNode(fileNode);
            node.add(fileTreeNode);
            
            if (file.isDirectory()) {
                loadFilesIntoNode(file, fileTreeNode);
            }
        }
    }
    
    private void toggleChildren(DefaultMutableTreeNode node, boolean selected) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (child.getUserObject() instanceof FileNode) {
                FileNode childFileNode = (FileNode) child.getUserObject();
                childFileNode.setSelected(selected);
                
                // Recursively toggle all children
                if (childFileNode.getFile().isDirectory()) {
                    toggleChildren(child, selected);
                }
            }
        }
    }
    
    public List<File> getSelectedFiles() {
        List<File> selectedFiles = new ArrayList<>();
        collectSelectedFiles(rootNode, selectedFiles);
        return selectedFiles;
    }
    
    private void collectSelectedFiles(DefaultMutableTreeNode node, List<File> selectedFiles) {
        int childCount = node.getChildCount();
        
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            Object userObject = childNode.getUserObject();
            
            if (userObject instanceof FileNode) {
                FileNode fileNode = (FileNode) userObject;
                if (fileNode.isSelected() && !fileNode.getFile().isDirectory()) {
                    selectedFiles.add(fileNode.getFile());
                }
            }
            
            collectSelectedFiles(childNode, selectedFiles);
        }
    }
    
    public void addFileSelectionListener(Consumer<File> listener) {
        fileSelectionListeners.add(listener);
    }
    
    // Class to hold file data and selection state
    private static class FileNode {
        private File file;
        private boolean selected;
        private boolean isDirectory;
        
        public FileNode(File file, boolean selected, boolean isDirectory) {
            this.file = file;
            this.selected = selected;
            this.isDirectory = isDirectory;
        }
        
        public File getFile() {
            return file;
        }
        
        public boolean isSelected() {
            return selected;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        public boolean isDirectory() {
            return isDirectory;
        }
    }
    
    // Custom renderer to show checkboxes in the tree
    private class CheckboxTreeCellRenderer extends DefaultTreeCellRenderer {
        private JCheckBox checkbox = new JCheckBox();
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
                                                     boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component renderer = super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);
            
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();
                
                if (userObject instanceof FileNode) {
                    FileNode fileNode = (FileNode) userObject;
                    checkbox.setSelected(fileNode.isSelected());
                    checkbox.setText(fileNode.getFile().getName());
                    checkbox.setOpaque(false);
                    checkbox.setForeground(renderer.getForeground());
                    checkbox.setBackground(renderer.getBackground());
                    checkbox.setFont(renderer.getFont());
                    return checkbox;
                }
            }
            
            return renderer;
        }
    }
    
    // Custom editor to enable checkbox selection
    private class CheckboxTreeCellEditor extends DefaultCellEditor {
        private JCheckBox checkbox;
        
        public CheckboxTreeCellEditor(JTree tree) {
            super(new JCheckBox());
            checkbox = (JCheckBox) getComponent();
        }
        
        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                   boolean expanded, boolean leaf, int row) {
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();
                
                if (userObject instanceof FileNode) {
                    FileNode fileNode = (FileNode) userObject;
                    checkbox.setSelected(fileNode.isSelected());
                    checkbox.setText(fileNode.getFile().getName());
                    return checkbox;
                }
            }
            
            return checkbox;
        }
    }
}