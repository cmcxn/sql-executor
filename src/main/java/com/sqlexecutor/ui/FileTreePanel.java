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
        fileTree = new JTree(treeModel);

        // 设置自定义渲染器
        fileTree.setCellRenderer(new CheckboxTreeCellRenderer());

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
                // 检测点击是否在复选框区域
                int checkboxWidth = 20;
                if (e.getX() < (rowBounds.x + checkboxWidth)) {
                    TreePath path = fileTree.getPathForRow(row);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                    if (node.getUserObject() instanceof FileNode) {
                        FileNode fileNode = (FileNode)node.getUserObject();
                        fileNode.setSelected(!fileNode.isSelected());

                        // 如果是目录，递归设置所有子项的选择状态
                        if (fileNode.getFile().isDirectory()) {
                            toggleChildren(node, fileNode.isSelected());
                        }

                        // 重绘树以显示更新后的复选框状态
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

        // 展开树以显示结构
        treeModel.reload();
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
    }

    private void loadFilesIntoNode(File folder, DefaultMutableTreeNode node) {
        File[] files = folder.listFiles(file ->
                file.isDirectory() || file.getName().toLowerCase().endsWith(".sql"));

        if (files == null) return;

        // 排序文件：目录优先，然后按字母顺序
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

                // 递归设置所有子项
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

    // 保存文件数据和选择状态的类
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

        @Override
        public String toString() {
            return file.getName();
        }
    }

    // 自定义的树节点渲染器，用于显示复选框
    private class CheckboxTreeCellRenderer extends DefaultTreeCellRenderer {
        private final JPanel panel = new JPanel();
        private final JCheckBox checkbox = new JCheckBox();
        private final JLabel label = new JLabel();

        public CheckboxTreeCellRenderer() {
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(checkbox);
            panel.add(Box.createHorizontalStrut(4)); // 添加一些间距
            panel.add(label);
            panel.setOpaque(false);

            // 禁用复选框的焦点，让它不接收焦点状态
            checkbox.setFocusable(false);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {

            // 获取默认渲染状态以更新颜色
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof FileNode) {
                    FileNode fileNode = (FileNode) userObject;

                    // 设置复选框状态
                    checkbox.setSelected(fileNode.isSelected());

                    // 设置文件名标签
                    label.setText(fileNode.getFile().getName());

                    // 复制背景色和前景色到panel及其组件
                    Color bg = selected ? getBackgroundSelectionColor() : getBackgroundNonSelectionColor();
                    Color fg = selected ? getTextSelectionColor() : getTextNonSelectionColor();

                    // 确保选中状态有合适的颜色
                    panel.setOpaque(true);
                    panel.setBackground(bg);

                    // 设置文本颜色
                    label.setForeground(fg);
                    checkbox.setForeground(fg);

                    // 设置图标
                    if (fileNode.isDirectory()) {
                        label.setIcon(expanded ? getOpenIcon() : getClosedIcon());
                    } else {
                        label.setIcon(getLeafIcon());
                    }

                    // 设置字体
                    label.setFont(getFont());

                    return panel;
                }
            }

            return this;
        }
    }
}