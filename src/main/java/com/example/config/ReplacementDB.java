package com.example.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.example.demo.Resources.Constants;

public class ReplacementDB {
    private final String sourceFilePath = Constants.sourceFilePath;
    private final String targetFilePath = Constants.targetFilePath;

    public void replacementDBEvent() {
        try {
            if(Files.exists(Path.of(sourceFilePath)))
            {
                Files.copy(Path.of(sourceFilePath), Path.of(targetFilePath), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(Path.of(sourceFilePath));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidSQLiteFile() {
        try (Connection conn = DriverManager.getConnection(Constants.connectionStringSqlite)) {
            return conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

}
