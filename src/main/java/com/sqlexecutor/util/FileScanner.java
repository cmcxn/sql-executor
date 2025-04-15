package com.sqlexecutor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileScanner {
    
    public static List<File> scanForSqlFiles(File directory) {
        List<File> sqlFiles = new ArrayList<>();
        
        if (directory == null || !directory.isDirectory()) {
            return sqlFiles;
        }
        
        scanDirectoryRecursively(directory, sqlFiles);
        
        // Sort files alphabetically
        sqlFiles.sort(Comparator.comparing(File::getName));
        
        return sqlFiles;
    }
    
    private static void scanDirectoryRecursively(File directory, List<File> sqlFiles) {
        File[] files = directory.listFiles();
        
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectoryRecursively(file, sqlFiles);
            } else if (isSqlFile(file)) {
                sqlFiles.add(file);
            }
        }
    }
    
    private static boolean isSqlFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".sql");
    }
}