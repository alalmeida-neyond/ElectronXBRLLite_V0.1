package com.example.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ReplacementDB {
    private final String sourceFilePath = System.getProperty("user.dir") + File.separator + "/src/UNMANAGEDPROCESS.db";
    private final String targetFilePath = System.getProperty("user.dir") + File.separator + "UNMANAGEDPROCESS.db";

    public void replacementDBEvent() {
        System.out.println("Replacement Called Inside Method");
        try {
            System.out.println("Replacement");
            if(Files.exists(Path.of(sourceFilePath)))
            {
                System.out.println("File Found");
                Files.copy(Path.of(sourceFilePath), Path.of(targetFilePath), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(Path.of(sourceFilePath));
                System.out.println("File Copied");
            }
            
        } catch (IOException e) {
            e.printStackTrace();

            System.err.println("Error replacing file: " + e.getMessage());
        }
        System.out.println("Early startup logic before context refresh or Hibernate init.");
    }

    public boolean isValidSQLiteFile() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:UNMANAGEDPROCESS.db")) {
            System.out.println("Connection made, meaning it's valid");
            return conn.isValid(1);
        } catch (SQLException e) {
            System.err.println("Invalid SQLite DB file: " + e.getMessage());
            return false;
        }
    }

}
