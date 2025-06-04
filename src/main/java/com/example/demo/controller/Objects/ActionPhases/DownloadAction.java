package com.example.demo.controller.Objects.ActionPhases;

import java.io.ByteArrayOutputStream;
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
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Comparator;


import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jboss.logging.Logger;

import com.example.demo.DTOs.OutValidationsDashboardDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Entities.DAL.OutValidationResultDAL;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.IO.IO;

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

    public void startDownload(String finalFolder, IO ioValidation) {
        SXSSFWorkbook validationsWorkbook = null;
        ByteArrayOutputStream bos_validations = null;
        try {

            List<OutValidationsDashboardDTO> lastValidationResult = getLastValidationResult();

            Path excelFilePath = Paths.get(finalFolder + Utils.getSeparator(), "ResultsValidation.csv");

            LOG.info("ExcelFilePath:" + excelFilePath.toString());

            OutValidationsDashboardDTO lastValidation = lastValidationResult.get(0);

            List<Object[]> validationData = new ArrayList<>();
            
            validationData = OutValidationResultDAL.getValidationResultsDetailsForGeneration(ioValidation);
            
            
            Object[] validationHeader = new Object[]{"Ref. Date", "Módulo", "Entidade", "Domínio", "Relatório", "Regra Código","Severidade", "Domínio Regra",
                 "Origem Regra", "Regra com valores", "Origem",	"Resultado", "Data processamento", "Diferença", "Margem"};
            Map<Integer, Object[]> validationHashmap = Utils.createHashMapForExcel(validationData, validationHeader);

            validationsWorkbook = new SXSSFWorkbook();
            validationsWorkbook = Utils.createSpreadSheet("Validações", validationHashmap, validationsWorkbook);

            String filenameValidations = "Validations_" + ioValidation.getEntity().getBdpId() + "_" + ioValidation.getModule().getCode().replace("_", "") + "_" + ioValidation.getDomain() + "_" + ioValidation.getReferenceDate() + ".xlsx";

            excelFilePath = Paths.get(finalFolder + Utils.getSeparator(), filenameValidations);
            try (FileOutputStream out = new FileOutputStream(excelFilePath.toString())) {
                validationsWorkbook.write(out);
            }

            Path zipFile = Paths.get(finalFolder + Utils.getSeparator(), finalFolder + ".zip");

            Path finalPackage = Paths.get(finalFolder + "_FinalPackage");
            Files.createDirectories(finalPackage);
            Path finalFolderPath = Paths.get(finalFolder);
            Path excelFileFinalPath = finalFolderPath.resolve(filenameValidations);
            Files.copy(excelFileFinalPath, finalPackage.resolve(excelFileFinalPath.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.delete(excelFileFinalPath);
            
            copyJSONs(finalFolder, ioValidation.getModule());

            organizeFiles(finalFolder);

            zipFolder(Paths.get(finalFolder));

            Path zipFilePath = finalFolderPath.getParent().resolve(finalFolderPath.getFileName() + ".zip");

            Path finalPackagePath = finalFolderPath.getParent()
                    .resolve(finalFolderPath.getFileName() + "_FinalPackage");

            Files.copy(zipFilePath, finalPackagePath.resolve(zipFile.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);

            LOG.info(finalPackage.toString());

            zipFolder(finalPackage);

            validationsWorkbook.close();

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
