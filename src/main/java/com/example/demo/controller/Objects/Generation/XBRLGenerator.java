/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jboss.logging.Logger;

import com.example.demo.DTOs.OutValidationsDashboardDTO;
import com.example.demo.Data.*;
import com.example.demo.Data.Access.Info;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Entities.Conf.*;
import com.example.demo.controller.Objects.Entities.DAL.*;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.Entities.DPMOrigin.TableVersionDPM;
import com.example.demo.controller.Objects.IO.IO;
import com.example.demo.controller.Objects.Import.*;
import com.example.demo.controller.Objects.Logs.GenerateLogDAL;
import com.example.demo.service.ProgressService;

public class XBRLGenerator implements Runnable {

    private List<InImportedTablesTemp> fileList;
    private String threadName;

    private ModuleVersion module;
    private String domain;
    private ConfEntities entity;
    private LocalDate referenceDate;

    private ProgressService progressService;

    private final Logger LOG = Logger.getLogger(XBRLGenerator.class);

    private boolean generationRunning;

    public XBRLGenerator(ProgressService progressService, LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity) {
        this.module = moduleVersion;
        this.progressService = progressService;
        this.referenceDate = referenceDate;
        this.domain = domain;
        this.entity = entity;
    }

    private final String folderUrl = Constants.folderUrl;

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
        Path pathDirectoryOffline = Paths.get("XBRL_Lite", "Run", "Reports", "JSON");

        String moduleVersionCode = moduleVersion.getCode();

        String moduleVersionCodeSearchJSON = moduleVersionCode.replace("_", "");

        Path destinationFileOffline = pathDirectoryOffline.resolve("reportPackage.json"); 
        Path destinationFile = Path.of(xbrlFolder).resolve("reportPackage.json");

        if (Files.exists(destinationFileOffline)) {
            try {
                Files.copy(destinationFileOffline, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("Erro I/O durante download de ficheiro:" + e.getMessage());                
                e.printStackTrace();
            } 
        } else {
            try {
                URL urlReportPackage = URI.create(folderUrl + "/reportPackage.json").toURL();
                try (InputStream inputStream = urlReportPackage.openStream()) {
                    Files.copy(inputStream, destinationFileOffline, StandardCopyOption.REPLACE_EXISTING);
                }
                try (InputStream inputStream = urlReportPackage.openStream()) {
                    Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (MalformedURLException exc) {
                LOG.error("Handle Invalido:" + exc.getMessage());
                exc.printStackTrace();
            } catch (IOException exc) {
                LOG.error("Erro I/O durante download de ficheiro:" + exc.getMessage());
                exc.printStackTrace();
            }
        }

        String moduleVersionCodeSearchJSONUpperCase = moduleVersionCodeSearchJSON.toUpperCase();
        Path destinationFileSpecificOffline = pathDirectoryOffline.resolve(moduleVersionCodeSearchJSONUpperCase + ".json"); 
        Path destinationFileSpecific = Path.of(xbrlFolder).resolve("report.json");

        if (Files.exists(destinationFileSpecificOffline)) {
            try {
                Files.copy(destinationFileSpecificOffline, destinationFileSpecific, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("Erro I/O durante download de ficheiro:" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            String correspondentJSON = getJSONDocument(moduleVersion);
            Path correspondentJSONOffline = pathDirectoryOffline.resolve(correspondentJSON);

            if (Files.exists(correspondentJSONOffline)) {
                try {
                    Files.copy(correspondentJSONOffline, destinationFileSpecificOffline, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOG.error("Erro I/O durante download de ficheiro:" + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                URI uriJSON;
                try {
                    uriJSON = URI.create(folderUrl + "/ModuleJSONs/" + correspondentJSON);
                    
                    try (InputStream inputStream = uriJSON.toURL().openStream()) {
                        Files.copy(inputStream, destinationFileSpecificOffline, StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch(FileNotFoundException exc)
                    {
                        LOG.error("Copia Nao Realizada:" + exc.getMessage());
                    }
                    try (InputStream inputStream = uriJSON.toURL().openStream()) {
                            Files.copy(inputStream, destinationFileSpecific, StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch(FileNotFoundException exc)
                    {
                        LOG.error("Ficheiro JSON Invalido:" + exc.getMessage());
                    }
                } catch (MalformedURLException exc) {
                    LOG.error("Handle Invalido JSON:" + exc.getMessage());
                    exc.printStackTrace();
                } catch (IOException exc) {
                    LOG.error("Erro I/O durante download de ficheiro JSON:" + exc.getMessage());
                    exc.printStackTrace();
                }
            }
        }
        
    }

    public String getJSONDocument(ModuleVersion moduleVersion)
    {
        JPA<ConfTemplate> jpa = new JPA<ConfTemplate>(ConfTemplate.class);
        List<ConfTemplate> result = new ArrayList<ConfTemplate>();

        try {
            StringBuilder queryString = new StringBuilder("select ct.*");
            queryString.append(" from CONF_TEMPLATE ct");
            queryString.append(" where ct.TEMPLATEID = :moduleVID ;");
            result = jpa.getTypedNativeResultList(queryString.toString(),"moduleVID", moduleVersion.getModuleVID());
            //result = jpa.getTypedNativeResultList(queryString.toString());

            /*result = jpa.getTypedNativeResultList(query.toString(),
                    "moduleVID", moduleVersion.getModuleVID());*/
            //ConfTemplate template = jpa.getSimpleResult("templateID", String.valueOf(moduleVersion.getModuleVID()));

            return result.getFirst().getJSONFileName();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        
        return null;
    }

    public void startDownload(String finalFolder, IO ioValidation) {
        SXSSFWorkbook validationsWorkbook = null;
        try {
            Path excelFilePath = Paths.get(finalFolder + Utils.getSeparator(), "ResultsValidation.csv");

            LOG.info("ExcelFilePath:" + excelFilePath.toString());

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

    public static File getLastModified(String directoryFilePath) {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        if (files != null) {
            for (File file : files) {
                if (file.lastModified() > lastModifiedTime) {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return chosenFile;
    }

    public void startGeneration(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename, IO ioImport, IO ioValidation) {
        List<IO> operationsRunningFromIO = IODAL.getOperationRunningFromIO(moduleVersion, domain, entity, referenceDate.toString());
        if (!operationsRunningFromIO.isEmpty()) {
            LOG.info(Constants.concurrentOperations + Constants.concurrentOperationsDesc);
            return;
        }

        String threadName = Constants.GEN + "_" + moduleVersion.getCode() + "_" + domain + "_"
                + referenceDate.toString() + "_" + entity.getBdpId();
        try {
            progressService.setGenerationProgress(25);
            xbrlGenerationMain(referenceDate,moduleVersion,domain,entity,ioImport, ioValidation, threadName);
        } catch (Exception e) {
            LOG.warn("Geracao | Erro na pesquisa logs geracao", e);
            return;
        }
        LOG.info(Constants.generationStarted + Constants.generationStartedDesc);

    }

    public boolean isGenerationRunning() {
        return generationRunning;
    }

    public void setGenerationRunning(boolean generationRunning) {
        this.generationRunning = generationRunning;
    }

    public void xbrlGenerationMain(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity,IO ioImport, IO ioValidation, String threadName) {

        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        
        StringBuilder query = new StringBuilder(" DELETE FROM IO WHERE actionid IN (1, 2, 3) ");

        query.append("AND modulevid = :moduleVID ")
                    .append("AND ioid NOT IN ( ")
                    .append("SELECT MAX(ioid) ")
                    .append("FROM IO ")
                    .append("WHERE actionid IN (1, 2, 3) ")
                    .append("AND modulevid = :moduleVID ")
                    .append("GROUP BY actionid ); ");
        
        try {
            jpa.executeNativeQuery(query.toString(),"moduleVID", moduleVersion != null ? String.valueOf(moduleVersion.getModuleVID()) : null);
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        }

        progressService.setGenerationProgress(50);

        
        IO generationIo = null;
        ConnectionManager em = null;
        String path = "";
        DateTimeFormatter formater = Constants.generationDateFormat;
        LocalDateTime now = LocalDateTime.now();
        String folderName = "";
        String finalFolder = "";
        File directory;
        try {
            em = new ConnectionManager();                

            path = Paths.get("XBRL_Lite","Run", "Reports", "XBRL_Generated").toString();
            
            folderName = getEntity().getLeiCode() + "." + getDomain() + "_PT_" + Utils.applyVersionString(getModule()) + "_" + getModule().getCode().replace("_", "") + "_" + getReferenceDate() + "_" + now.format(formater);
            //Create folder
            finalFolder = path + Utils.getSeparator() + folderName;
            directory = new File(finalFolder);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            generationIo = new IO(
                    Info.getInstance().getIOStateByID(Constants.processoPending),
                    getReferenceDate(),
                    getModule(),
                    domain,
                    getEntity(),
                    LocalDateTime.now(),
                    null,
                    Info.getInstance().getConfActionByID(Integer.valueOf(Constants.actionGeneration)),//new ConfAction(Constants.actionImport),
                    "ABC",
                    folderName,
                    threadName,
                    folderName
            );

            Connection.persist(em, generationIo);

            OutXBRLGenerated outXBRLGenerated = new OutXBRLGenerated("ABC", folderName, getModule(), now, getEntity(), domain, getReferenceDate(), ioImport);

            Connection.persist(em, outXBRLGenerated);
            GenerateLogDAL.createNewGenerationLog("Geracao Iniciada com sucesso", outXBRLGenerated.getIdXBRLGenerate());
            //Get Object from NULL
            boolean altGeneration = Info.getInstance().checkIfUsesAltGeneration(module.getModuleVID(),Constants.GENERATIONBASEDONCOLLUMN);
            List<InImportedTablesTemp> tempList = InImportedTablesDAL.getListOfImportedMaps(module, referenceDate, entity, domain, ioImport);

            //Create Map
            Map<String, List<InImportedTablesTemp>> listOfTableGroupedByTheTableVID = tempList.stream()
                    .collect(Collectors.groupingBy(item -> item.getTableVersion().getCode()));
            Queue<Thread> threadList = new LinkedList<>();
            //HERE
            for (Map.Entry<String, List<InImportedTablesTemp>> entry : listOfTableGroupedByTheTableVID.entrySet()) {

                Thread t = new Thread(new XBRLGeneratorMap(finalFolder, entry.getValue(), ioImport.getReferenceDate(), altGeneration));
                threadList.add(t);
                t.start();
            }
            progressService.setGenerationProgress(75);
            createParametersCSV(finalFolder);
            Set<String> filteredMapWithoutEmpty = listOfTableGroupedByTheTableVID.entrySet().stream()
                    .filter(entry -> {
                        InImportedTablesTemp obj = entry.getValue().get(0);
                        return obj.getIoState().getIoStateId() != 12; 
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            progressService.setGenerationProgress(87);
            createFillingIndicatorCSV(finalFolder, filteredMapWithoutEmpty);
            boolean waitingForAllThread = true;
            Thread auxVariableToCheck = null;
            while (waitingForAllThread) {
                if (auxVariableToCheck == null) {
                    auxVariableToCheck = threadList.poll();
                    if (auxVariableToCheck == null && threadList.isEmpty()) {
                        waitingForAllThread = false;
                    }
                } else if (!auxVariableToCheck.isAlive()) {
                    auxVariableToCheck = null;
                }
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
            if (waitingForAllThread) {
                //Cancel Happen
                if (auxVariableToCheck != null) {
                    auxVariableToCheck.interrupt();
                }
                while (!threadList.isEmpty()) {
                    auxVariableToCheck = threadList.poll();
                    auxVariableToCheck.interrupt();
                }
                //Add to the Log Generation Canceled
                GenerateLogDAL.createNewGenerationLog("Geracao Cancelada com sucesso", outXBRLGenerated.getIdXBRLGenerate());
                generationIo.setEndTimestamp(LocalDateTime.now());
                generationIo.setIoState(Info.getInstance().getIOStateByID(Constants.processoCanceled));
                Connection.merge(generationIo);  
                //setGenerationProgress(100);  

            } else {
                //Add to the Log Generation Conclude sucesufully
                GenerateLogDAL.createNewGenerationLog("Geracao Concluída com sucesso", outXBRLGenerated.getIdXBRLGenerate());
                generationIo.setEndTimestamp(LocalDateTime.now());
                generationIo.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));
                Connection.merge(generationIo);                
            }

        } catch (Exception e) {
            e.printStackTrace();
            if(generationIo != null)
            {
                generationIo.setEndTimestamp(LocalDateTime.now());
                generationIo.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));
            }
            
            Connection.merge(generationIo); 
        }

        progressService.setGenerationProgress(100);
        startDownload(finalFolder, ioValidation);
    }

    public void createFillingIndicatorCSV(String path, Set<String> maps) {
        List<TableVersionDPM> fillingIndicatorModuleList = TableVersionDAL.getAllFilesImported(getModule(), getReferenceDate());
        //Path fillingCSVPath = Paths.get(path + Utils.getSeparator(), "FilingIndicators.csv");
        Path fillingCSVPath = Paths.get(path + Utils.getSeparator(), Constants.FILLINGINDICATORSFILENAME);
        Map<String, Boolean> mapNameChecker = new TreeMap<>();
        Map<String, String> abstractNameChecker = new HashMap<>();
        for (TableVersionDPM tableVersion : fillingIndicatorModuleList) {
            if (tableVersion.getAbstractTable() == null) {
                mapNameChecker.put(tableVersion.getCode(), false);
            } else {
                String auxStringName = tableVersion.getCode();
                for (TableVersionDPM tableVersionInside : fillingIndicatorModuleList) {
                    if (tableVersionInside.getTable().getTableId() == tableVersion.getAbstractTable().getTableId()) {
                        auxStringName = tableVersionInside.getCode();
                        break;
                    }
                }
                abstractNameChecker.put(tableVersion.getCode(), auxStringName);
            }
        }
        for (String map : maps) {
            if (mapNameChecker.containsKey(map)) {
                mapNameChecker.replace(map, Boolean.TRUE);
            } else {
                if (abstractNameChecker.containsKey(map)) {
                    mapNameChecker.replace(abstractNameChecker.get(map), Boolean.TRUE);
                }
            }
        }

        try (FileWriter writer = new FileWriter(fillingCSVPath.toFile())) {
            //writer.append("templateID,reported");
            writer.append(Constants.FILLINGINDICATORSLABELS);
            for (Map.Entry<String, Boolean> entry : mapNameChecker.entrySet()) {
                writer.append("\n");
                writer.append(entry.getKey());
                writer.append(",");
                //writer.append((entry.getValue() ? "true" : "false"));
                writer.append((entry.getValue() ? Constants.TRUERESULT : Constants.FALSERESULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createParametersCSV(String path) {
        /*Path paramCSVPath = Paths.get(path + Utils.getSeparator(), "parameters.csv");
        String currency = ConfAppConfigsDAL.getValueOfAppConfigurationKey("CURRENCY");
        String monetary = ConfAppConfigsDAL.getValueOfAppConfigurationKey("MONETARY");
        String percentage = ConfAppConfigsDAL.getValueOfAppConfigurationKey("PERCENTAGE");
        String decimal = ConfAppConfigsDAL.getValueOfAppConfigurationKey("DECIMAL");
        String integer = ConfAppConfigsDAL.getValueOfAppConfigurationKey("INTEGER");*/
        Path paramCSVPath = Paths.get(path + Utils.getSeparator(), Constants.PARAMETERSFILENAME);
        String currency = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGCURRENCY);
        String monetary = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGMONETARY);
        String percentage = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGPERCENTAGE);
        String decimal = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGDECIMAL);
        String integer = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGINTEGER);
        try (FileWriter writer = new FileWriter(paramCSVPath.toFile())) {
            //writer.append("name,value\n");
            writer.append(Constants.PARAMETERSLABELS);
            writer.append("\n");
            //writer.append("entityID,");
            writer.append(Constants.PARAMETERSKEYENTITY);
            writer.append(getEntity().getLeiCode());
            writer.append(".");
            writer.append(domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase());
            writer.append("\n");
            //writer.append("refPeriod,");
            writer.append(Constants.PARAMETERSKEYREFERENCEDATE);
            writer.append(getReferenceDate().toString()); 
            /*writer.append("\n");
            writer.append("baseCurrency,");
            writer.append(currency);
            writer.append("\n");
            writer.append("decimalsInteger,");
            writer.append(integer);
            writer.append("\n");
            writer.append("decimalsMonetary,");
            writer.append(monetary);
            writer.append("\n");
            writer.append("decimalsPercentage,");
            writer.append(percentage);
            writer.append("\n");
            writer.append("decimalsDecimal,");
            writer.append(decimal);*/
            writer.append("\n");
            writer.append(Constants.PARAMETERSKEYCURRENCY);
            writer.append(currency);
            writer.append("\n");
            writer.append(Constants.PARAMETERSKEYINTEGER);
            writer.append(integer);
            writer.append("\n");
            writer.append(Constants.PARAMETERSKEYMONETARY);
            writer.append(monetary);
            writer.append("\n");
            writer.append(Constants.PARAMETERSKEYPERCENTAGE);
            writer.append(percentage);
            writer.append("\n");
            writer.append(Constants.PARAMETERSKEYDECIMAL);
            writer.append(decimal);
        } catch (IOException e) {
            
        }
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public List<InImportedTablesTemp> getFileList() {
        return fileList;
    }

    public void setFileList(List<InImportedTablesTemp> fileList) {
        this.fileList = fileList;
    }

    public ModuleVersion getModule() {
        return module;
    }

    public void setModule(ModuleVersion module) {
        this.module = module;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public ConfEntities getEntity() {
        return entity;
    }

    public void setEntity(ConfEntities entity) {
        this.entity = entity;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    @Override
    public void run() {
    }
}
