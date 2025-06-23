package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;


@Controller
@RequestMapping("/file")
public class FileController {
    // Get the project directory dynamically
    private static final String UPLOAD_DIR = System.getProperty("user.dir");
    private String directory;

    private final String fileUrl = "https://blobstoragexbrl.blob.core.windows.net/xbrldatabaseblob/UNMANAGEDPROCESS.db";
    private final String localFilePath = System.getProperty("user.dir") + File.separator + "/src/UNMANAGEDPROCESS.db"; 

    @GetMapping("/upload")
    public ModelAndView showUploadForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("upload");
        return modelAndView; 
    }

    @PostMapping("/process")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        StringBuilder responseMessage = new StringBuilder();

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            return "<p style='color:red;'>Tipo de ficheiro inválido</p>";
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                return "<p style='color:red;'>Diretoria inválida</p>";
            }
            /*
                Ver quantas colunas tem o documento e fazer a mesma quantidade de arrays que tem de linhas
                Depois em cada array colocar a informacao das colunas
                Depois colocar esses arrays num array global
                Depois iteramos o array global
            */ 
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            List<String> extractedDataColumn1 = extractExcelColumn(filePath, 1);
            List<String> extractedDataColumn2 = extractExcelColumn(filePath, 1);
            List<String[]> fullList = new ArrayList<>();
            int i = 0;
            for (String extractedDataLine : extractedDataColumn1) {
                fullList.add(new String[]{extractedDataLine, extractedDataColumn2.get(i)});
                i++;
            }
            
            insertIntoDatabase(fullList);
            
            
            responseMessage.append("<p style='color:green;'>Ficheiro carrgado com sucesso!</p>");
            
        } catch (Exception e) {
            return "<p style='color:red;'>Erro a processar o ficheiro: " + e.getMessage() + "</p>";
        }
        return responseMessage.toString();
    }

    private List<String> extractExcelColumn(Path filePath, int columnIndex) {
        List<String> columnValues = new ArrayList<>();

        return columnValues;
    }

    private void insertIntoDatabase(List<String[]> columnValues) {
        directory = "jdbc:sqlite:UNMANAGEDPROCESS.db";
        String sqlQuery = "INSERT INTO tabelaTeste (Name, Description) VALUES (?, ?);";

        try (Connection conn = DriverManager.getConnection(directory);
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            for (String[] value : columnValues) {
                pstmt.setString(1, value[0]);
                pstmt.setString(2, value[1]);
                pstmt.executeUpdate(); // Insert row into SQLite
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/processMetaData")
    public void handleMetaDataTransfer(Model model) {
        try {
            
            URL url = URI.create(fileUrl).toURL();
            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, Path.of(localFilePath), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error replacing file: " + e.getMessage());
        }
    }
}
