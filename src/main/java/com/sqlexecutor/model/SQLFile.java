package com.sqlexecutor.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SQLFile {
    private File file;
    private boolean selected;
    private String content;

    public SQLFile(File file) {
        this.file = file;
        this.selected = false;
        loadContent();
    }

    private void loadContent() {
        try {
            this.content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        } catch (IOException e) {
            this.content = "Error loading file: " + e.getMessage();
        }
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getName();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }
}