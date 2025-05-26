package com.example.demo.controller.Objects.ActionPhases;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Comparator;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

import com.example.demo.DTOs.OutValidationsDashboardDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.Utils;
import com.example.demo.controller.Objects.Entities.IO;
import com.example.demo.controller.Objects.Entities.ModuleVersion;

public class DownloadAction {
    private final Logger LOG = Logger.getLogger(DownloadAction.class);

    private final String folderUrl = "https://blobstoragexbrl.blob.core.windows.net/xbrldatabaseblob/JSONs";

    public List<OutValidationsDashboardDTO> getLastValidationResult() {
        JPA<OutValidationsDashboardDTO> jpa = new JPA<OutValidationsDashboardDTO>(OutValidationsDashboardDTO.class);
        List<OutValidationsDashboardDTO> validationResultsDashboard = new ArrayList<>();

        try {
            StringBuilder queryString = new StringBuilder("SELECT REFERENCEDATE AS refDate, ");

            queryString.append("MODULEVID AS module, ")
                    .append("DOMAIN AS domain, ")
                    .append("ENTITYID AS entity, ")
                    .append("CASE WHEN MANDATORYREPORTSIMPORTED = 'Y' THEN 1 ELSE 0 END AS mandatoryReportsImported, ")
                    .append("OK AS Ok, ")
                    .append("DNRR AS dnrr, ")
                    .append("ERROR AS error, ")
                    .append("WARNING AS warning, ")
                    .append("PROCESSNOTOK AS processNotOk, ")
                    .append("TOTAL AS total, ")
                    .append("EXPECTEDTORUN AS expectedToRun, ")
                    .append("TIMESTAMP AS timestamp, ")
                    .append("CASE WHEN OK = TOTAL THEN 1 ELSE 0 END AS allValidated ")
                    .append("FROM OUT_VALIDATIONSDASHBOARD ")
                    .append("ORDER BY timestamp DESC ")
                    .append("LIMIT 1");
            validationResultsDashboard = jpa.getNativeResultListWithMapping(queryString.toString(),
                    "ValidationsDashboardResults");

        } catch (Exception e) {
            LOG.error("Erro na query ValidationResults no Download XBRL:" + e.getMessage());
            e.printStackTrace();
        } finally {
            jpa.close();
        }
        return validationResultsDashboard;
    }

    private void copyJSONs(String xbrlFolder, ModuleVersion moduleVersion) {
        LOG.info("Module Version Name:" + moduleVersion.getCode());
        Path destinationFile = Path.of(xbrlFolder).resolve("reportPackage.json");
        try {
            URL urlReportPackage = URI.create(folderUrl + "/reportPackage.json").toURL();
            try (InputStream inputStream = urlReportPackage.openStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (MalformedURLException e) {
            LOG.error("Handle Invalido:" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Erro I/O durante download de ficheiro:" + e.getMessage());
            e.printStackTrace();
        }

        String moduleVersionCode = moduleVersion.getCode();

        String moduleVersionCodeSearchJSON = moduleVersionCode.replace("_", "");

        Path destinationFileSpecific = Path.of(xbrlFolder).resolve("report.json");

        String moduleVersionCodeSearchJSONUpperCase = moduleVersionCodeSearchJSON.toUpperCase();

        try {
            URL urlJSON = URI.create(folderUrl + "/ModuleJSONs/" + moduleVersionCodeSearchJSONUpperCase + ".json").toURL();
            try (InputStream inputStream = urlJSON.openStream()) {
                Files.copy(inputStream, destinationFileSpecific, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (MalformedURLException e) {
            LOG.error("Handle Invalido JSON:" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Erro I/O durante download de ficheiro JSON:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startDownload(String finalFolder, IO io) {
        try {

            List<OutValidationsDashboardDTO> lastValidationResult = getLastValidationResult();

            Path excelFilePath = Paths.get(finalFolder + Utils.getSeparator(), "ResultsValidation.csv");

            LOG.info("ExcelFilePath:" + excelFilePath.toString());

            OutValidationsDashboardDTO lastValidation = lastValidationResult.get(0);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet("Results");

            Row headerRow = sheet.createRow(0);

            headerRow.createCell(0).setCellValue("Reference Date");
            headerRow.createCell(1).setCellValue("Module");
            headerRow.createCell(2).setCellValue("Domain");
            headerRow.createCell(3).setCellValue("Entity");
            headerRow.createCell(4).setCellValue("Mandatory Reports Imported");
            headerRow.createCell(5).setCellValue("Rules OK");
            headerRow.createCell(6).setCellValue("Do Not Run Rules");
            headerRow.createCell(7).setCellValue("Rules Error");
            headerRow.createCell(8).setCellValue("Rules Not OK");
            headerRow.createCell(9).setCellValue("Total Rules");
            headerRow.createCell(10).setCellValue("Rules Expected to Run");
            headerRow.createCell(11).setCellValue("Timestamp");

            Row valueRow = sheet.createRow(1);

            valueRow.createCell(0).setCellValue(lastValidation.getRefDate().toString());
            valueRow.createCell(1).setCellValue(lastValidation.getModule());
            valueRow.createCell(2).setCellValue(lastValidation.getDomain());
            valueRow.createCell(3).setCellValue(lastValidation.getEntity());
            valueRow.createCell(4).setCellValue(lastValidation.getMandatoryReportsImported());
            valueRow.createCell(5).setCellValue(lastValidation.getNOk());
            valueRow.createCell(6).setCellValue(lastValidation.getNDNRR());
            valueRow.createCell(7).setCellValue(lastValidation.getNError());
            valueRow.createCell(8).setCellValue(lastValidation.getNProcessNotOk());
            valueRow.createCell(9).setCellValue(lastValidation.getNTotal());
            valueRow.createCell(10).setCellValue(lastValidation.getNExpectedToRun());
            valueRow.createCell(11).setCellValue(lastValidation.getTimestamp());

            try (FileOutputStream out = new FileOutputStream(excelFilePath.toString())) {
                workbook.write(out);
            }

            Path zipFile = Paths.get(finalFolder + Utils.getSeparator(), finalFolder + ".zip");

            Path finalPackage = Paths.get(finalFolder + "_FinalPackage");
            Files.createDirectories(finalPackage);
            Path finalFolderPath = Paths.get(finalFolder);
            Path excelFileFinalPath = finalFolderPath.resolve("ResultsValidation.csv");
            Files.copy(excelFileFinalPath, finalPackage.resolve(excelFileFinalPath.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.delete(excelFileFinalPath);
            
            copyJSONs(finalFolder, io.getModule());

            organizeFiles(finalFolder);

            zipFolder(Paths.get(finalFolder));

            Path zipFilePath = finalFolderPath.getParent().resolve(finalFolderPath.getFileName() + ".zip");

            Path finalPackagePath = finalFolderPath.getParent()
                    .resolve(finalFolderPath.getFileName() + "_FinalPackage");

            Files.copy(zipFilePath, finalPackagePath.resolve(zipFile.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);

            LOG.info(finalPackage.toString());

            zipFolder(finalPackage);

            workbook.close();

            cleanUp();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanUp()
    {
        Path pathDirectory = Paths.get("XBRL_Lite", "Run", "Reports", "XBRL_Generated");

        try {
            Files.walk(pathDirectory)
                 .sorted(Comparator.reverseOrder())
                 .forEach(path -> {
                     try {
                         // Skip files or directories containing "_FinalPackage.zip"
                         if (!path.getFileName().toString().contains("_FinalPackage.zip")) {
                             Files.delete(path);
                         } 
                     } catch (IOException e) {
                         System.err.println("Problema a apagar: " + path + " (" + e.getMessage() + ")");
                     }
                 });
        } catch (IOException e) {
            LOG.error("Erro na limpeza de ficheiros");
            e.printStackTrace();
        }
    }

    private void organizeFiles(String xbrlFolder)
    {
        Path folderPath = Paths.get(xbrlFolder);
        Path metaINF = folderPath.resolve("META-INF");
        Path reports = folderPath.resolve("reports");

        try {
            Files.createDirectory(metaINF);
        } catch (FileAlreadyExistsException e) {
            LOG.error("Pasta ja existe: " + metaINF);
        } catch (IOException e) {
            LOG.error("Erro criar pasta: " + e.getMessage());
        }

        try {
            Files.createDirectory(reports);
        } catch (FileAlreadyExistsException e) {
            LOG.error("Pasta ja existe: " + reports);
        } catch (IOException e) {
            LOG.error("Erro criar pasta: " + e.getMessage());
        }
        
        Path folderMetaINF = Paths.get(folderPath.toString() + "/META-INF");
        Path folderReports = Paths.get(folderPath.toString() + "/reports");
        Path reportPackageJSON = folderPath.resolve("reportPackage.json");

        try {
            Files.move(reportPackageJSON, folderMetaINF.resolve(reportPackageJSON.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("Erro a mover ficheiro reportPackage.json:" + e.getMessage());
            e.printStackTrace();
        }
                
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    Files.move(file, folderReports.resolve(file.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            LOG.error("Error reading directory: " + e.getMessage());
        }
    }

    public static void zipFolder(Path sourceFolderPath) throws IOException {
        if (!Files.isDirectory(sourceFolderPath)) {
            throw new IllegalArgumentException("Diretoria Invalida");
        }

        Path parentDir = sourceFolderPath.getParent();
        String folderName = sourceFolderPath.getFileName().toString();
        Path zipFilePath = parentDir.resolve(folderName + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            Files.walk(sourceFolderPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            String entryName = sourceFolderPath.relativize(path).toString().replace("\\", "/");
                            zos.putNextEntry(new ZipEntry(entryName));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

    }
}
