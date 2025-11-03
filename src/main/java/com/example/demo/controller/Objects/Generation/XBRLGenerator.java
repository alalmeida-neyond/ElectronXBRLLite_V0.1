/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Generation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jboss.logging.Logger;

import com.example.demo.DTOs.DataTypeHasUnitDTO;
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
import com.example.demo.controller.Objects.IO.IOState;
import com.example.demo.controller.Objects.Import.*;
import com.example.demo.controller.Objects.Logs.GenerateLogDAL;
import com.example.demo.service.ProgressService;

import jakarta.persistence.EntityManager;

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
    
    public void xbrlGenerationMain(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity,IO ioImport, IO ioValidation, String threadName) {
        IO generationIo = null;
        ConnectionManager em = null;
        String path = "";
        DateTimeFormatter formater = Constants.generationDateFormat;
        LocalDateTime now = LocalDateTime.now();
        String folderName = "";
        String finalFolder = "";
        File directory;
        try {
            int completedSteps = 1;
            em = new ConnectionManager(Connection.getEm());                

            path = Paths.get("XBRL_Lite","Run", "Reports", "XBRL_Generated").toString();
            
            //folderName = getEntity().getLeiCode() + "." + getDomain() + "_PT_" + Utils.applyVersionString(getModule()) + "_" + getModule().getCode().replace("_", "") + "_" + getReferenceDate() + "_" + now.format(formater);
            
            folderName = getEntity().getBdpId() + "." +  getDomain().toUpperCase() + "." + getReferenceDate().format(Constants.dateFormatFileName) + "." + getModule().getCode();

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
                    Info.getInstance().getConfActionByID(Integer.valueOf(Constants.actionGeneration)),
                    "Generatate",
                    folderName,
                    threadName,
                    folderName
            );

            Connection.persist(em, generationIo);

            OutXBRLGenerated outXBRLGenerated = new OutXBRLGenerated("Generatate", folderName, getModule(), now, getEntity(), domain, getReferenceDate(), generationIo);
            Connection.persist(em, outXBRLGenerated);

            GenerateLogDAL.createNewGenerationLog(Constants.generationStartedDesc, outXBRLGenerated.getIdXBRLGenerate(), em);
            //Get Object from NULL
            boolean altGeneration = Info.getInstance().checkIfUsesAltGeneration(module.getModuleVID(),Constants.GENERATIONBASEDONCOLLUMN);
            List<InImportedTablesTemp> tempList = InImportedTablesDAL.getListOfImportedMaps(module, referenceDate, entity, domain, ioImport);
            progressService.setGenerationProgress(completedSteps,tempList.size() + 3);

            //Create Report JSON
            boolean wasReportsJsonCreatedSucessufuly = createReportJSON(finalFolder, getModule().getModuleVID());
            if (!wasReportsJsonCreatedSucessufuly){
                GenerateLogDAL.createNewGenerationLog(Constants.generationConfigError, outXBRLGenerated.getIdXBRLGenerate(), em);
                generationIo.setEndTimestamp(LocalDateTime.now());
                generationIo.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));
                Connection.merge(em,generationIo);      
                return;
            }
            
            // Create Parameter CSV
            List<DataTypeHasUnitDTO> importedDatatypes = InImportedTablesDAL.getListOfImportedDatatypes(module, referenceDate, entity, domain);
            createParametersCSV(finalFolder, importedDatatypes);
            completedSteps++;
            progressService.setGenerationProgress(completedSteps,tempList.size() + 3);

            //Create Map
            Map<String, List<InImportedTablesTemp>> listOfTableGroupedByTheTableVID = tempList.stream()
                    .collect(Collectors.groupingBy(item -> item.getTableVersion().getCode()));
            for (Map.Entry<String, List<InImportedTablesTemp>> entry : listOfTableGroupedByTheTableVID.entrySet()) {
                XBRLGeneratorMap.populateCSV(
                        finalFolder, 
                        entry.getValue(), 
                        generationIo.getReferenceDate(), 
                        altGeneration
                );
            }
            completedSteps++;
            progressService.setGenerationProgress(completedSteps,listOfTableGroupedByTheTableVID.size() + 3);
            //HERE
            
            //Filling Indicators
            Set<String> filteredMapWithoutEmpty = listOfTableGroupedByTheTableVID.entrySet().stream()
                    .filter(entry -> {
                        InImportedTablesTemp obj = entry.getValue().get(0);
                        return obj.getIoState().getIoStateId() != 12; 
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            completedSteps++;
            progressService.setGenerationProgress(completedSteps,listOfTableGroupedByTheTableVID.size() + 3);
            createFillingIndicatorCSV(finalFolder, filteredMapWithoutEmpty);
           GenerateLogDAL.createNewGenerationLog(Constants.generationEndedDesc, outXBRLGenerated.getIdXBRLGenerate(), em);

        } catch (Exception e) {
            e.printStackTrace();
            if(generationIo != null)
            {
                generationIo.setEndTimestamp(LocalDateTime.now());
                generationIo.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));
            }
            
            Connection.merge(generationIo); 
        } 

        startDownload(finalFolder, ioValidation, generationIo);
    }

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

    public void startDownload(String finalFolder, IO ioValidation, IO ioGeneration) {
        SXSSFWorkbook validationsWorkbook = null;
        EntityManager em = Connection.getEm();
        ConnectionManager cm = null;
        try {
            cm = new ConnectionManager(em);
            Path excelFilePath = Paths.get(finalFolder + Utils.getSeparator(), "ResultsValidation.csv");


            List<Object[]> validationData = new ArrayList<>();
            
            validationData = OutValidationResultDAL.getValidationResultsDetailsForGeneration(
            ioValidation.getIoId(),
            ioValidation.getModule(), 
            ioValidation.getReferenceDate(),
            ioValidation.getEntity(), 
            ioValidation.getDomain());

            Object[] validationHeader = new Object[]{"Módulo", "Entidade", "Relatório", "Domínio", "Ref. Date", "Regra Código", "Origem Regra", "Severidade", "Origem", "Domínio Regra",
                "Regra com valores", "Resultado", "Data processamento", "Diferença", "Margem"};
            Map<Integer, Object[]> validationHashmap = Utils.createHashMapForExcel(validationData, validationHeader);

            validationsWorkbook = new SXSSFWorkbook();
            validationsWorkbook = Utils.createSpreadSheet("Validações", validationHashmap, validationsWorkbook);

            String filenameValidations = "Validations_" + ioValidation.getEntity().getBdpId() + "_" + ioValidation.getModule().getCode().replace("_", "") + "_" + ioValidation.getDomain() + "_" + ioValidation.getReferenceDate() + ".xlsx";

            excelFilePath = Paths.get(finalFolder + Utils.getSeparator(), filenameValidations);
            try (FileOutputStream out = new FileOutputStream(excelFilePath.toString())) {
                validationsWorkbook.write(out);
            }

            Path zipFile = Paths.get(finalFolder + Utils.getSeparator(), finalFolder + ".zip");

            Path finalPackage = Paths.get(finalFolder);
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
                    .resolve(finalFolderPath.getFileName());

            Files.copy(zipFilePath, finalPackagePath.resolve(zipFile.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);

            zipFolder(finalPackage);

            copyZipToPreferedDirectory(finalPackagePath);
            

            validationsWorkbook.close();

            progressService.setGenerationProgress(1, 1);
            IOState ioState = Info.getInstance().getIOStateByID(Constants.processoOk);
            ioGeneration.setIoState(ioState);
            ioGeneration.setEndTimestamp(LocalDateTime.now());
            //Connection.merge(io);
            Connection.merge(cm, ioGeneration);

        } catch (Exception e) {
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

    public Path getPreferredDirectory() {
        Path pathFile = Paths.get("path.dat");
        String folderPath = null;

        try {
            if (Files.exists(pathFile)) {
                folderPath = Files.readString(pathFile, StandardCharsets.UTF_8).trim();
            }

            // If file is empty or does not exist, use Downloads
            if (folderPath == null || folderPath.isEmpty()) {
                folderPath = System.getProperty("user.home") + File.separator + "Downloads";
            }

            Path preferredPath = Paths.get(folderPath);
            return preferredPath;

        } catch (IOException e) {
            LOG.error("Failed to read path.dat, defaulting to Downloads folder", e);
            return Paths.get(System.getProperty("user.home"), "Downloads");
        }
    }


    public void copyZipToPreferedDirectory(Path zipDirectory) {
        if (!Files.isDirectory(zipDirectory)) {
            throw new IllegalArgumentException("Diretoria Invalida");
        }

        try {
            Path preferredDirectory = getPreferredDirectory();

            Path zipFile = Paths.get(zipDirectory.toString() + ".zip");

            Path targetFile = preferredDirectory.resolve(zipFile.getFileName());
            
            Files.copy(zipFile, targetFile,
                    StandardCopyOption.COPY_ATTRIBUTES,
                    StandardCopyOption.REPLACE_EXISTING);


        } catch (IOException e) {
            LOG.error("Erro ao copiar o ficheiro ZIP para a diretoria preferida", e);
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
            return;
        }

        String threadName = Constants.GEN + "_" + moduleVersion.getCode() + "_" + domain + "_"
                + referenceDate.toString() + "_" + entity.getBdpId();
        try {
            xbrlGenerationMain(referenceDate,moduleVersion,domain,entity,ioImport, ioValidation, threadName);
        } catch (Exception e) {
            LOG.warn("Geracao | Erro na pesquisa logs geracao", e);
            return;
        }

    }

    public boolean isGenerationRunning() {
        return generationRunning;
    }

    public void setGenerationRunning(boolean generationRunning) {
        this.generationRunning = generationRunning;
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
            } else  if (abstractNameChecker.containsKey(map)) {
                mapNameChecker.replace(abstractNameChecker.get(map), Boolean.TRUE);
            }
        }

        try (FileWriter writer = new FileWriter(fillingCSVPath.toFile())) {
            writer.append(Constants.FILLINGINDICATORSLABELS);
            for (Map.Entry<String, Boolean> entry : mapNameChecker.entrySet()) {
                writer.append("\n");
                writer.append(entry.getKey());
                writer.append(",");
                writer.append((entry.getValue() ? Constants.TRUERESULT : Constants.FALSERESULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, Map.Entry<String, String>> createParametersMap(){
        String monetary = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGMONETARY);
        String percentage = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGPERCENTAGE);
        String decimal = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGDECIMAL);
        String integer = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGINTEGER);
        
        Map<Integer, Map.Entry<String, String>> parametersMap = new HashMap<>();
        parametersMap.put(Constants.DATATYPEDECIMAL, new AbstractMap.SimpleEntry<>(Constants.PARAMETERSKEYDECIMAL, decimal));
        parametersMap.put(Constants.DATATYPEINTEGER, new AbstractMap.SimpleEntry<>(Constants.PARAMETERSKEYINTEGER, integer));
        parametersMap.put(Constants.DATATYPEPERCENTAGE, new AbstractMap.SimpleEntry<>(Constants.PARAMETERSKEYPERCENTAGE, percentage));
        parametersMap.put(Constants.DATATYPEMONETARY, new AbstractMap.SimpleEntry<>(Constants.PARAMETERSKEYMONETARY, monetary));
        
        return parametersMap;
    }

    private void createParametersCSV(String path, List<DataTypeHasUnitDTO> importedDatatypes) {
        Map<Integer, Map.Entry<String, String>> parametersMap = createParametersMap();
        String currency = ConfAppConfigsDAL.getValueOfAppConfigurationKey(Constants.APPCONFIGCURRENCY);
                
        Path paramCSVPath = Paths.get(path + Utils.getSeparator(), Constants.PARAMETERSFILENAME);
        try (FileWriter writer = new FileWriter(paramCSVPath.toFile())) {
            //Fixed Parameters
            writer.append(Constants.PARAMETERSLABELS);
            writer.append("\n");
            writer.append(Constants.PARAMETERSKEYENTITY);
            writer.append(getEntity().getLeiCode());
            writer.append(".");
            writer.append(domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase());
            writer.append("\n");
            writer.append(Constants.PARAMETERSKEYREFERENCEDATE);
            writer.append(getReferenceDate().toString()); 
            writer.append("\n");
            
            boolean isMonetaryAlreadyFound = false;
            for(DataTypeHasUnitDTO dataType: importedDatatypes){
                Map.Entry<String, String> valuesToWrite = parametersMap.get(dataType.getDatatypeId());
                if(valuesToWrite == null) continue;
                
                if(dataType.getDatatypeId() != Constants.DATATYPEMONETARY || !isMonetaryAlreadyFound){
                    writer.append(valuesToWrite.getKey());
                    writer.append(valuesToWrite.getValue());
                    writer.append("\n");
                }
                if(dataType.getDatatypeId() == Constants.DATATYPEMONETARY && !dataType.getHasUnit()){
                    writer.append(Constants.PARAMETERSKEYCURRENCY);
                    writer.append(currency);
                    writer.append("\n");
                }
                if(dataType.getDatatypeId() == Constants.DATATYPEMONETARY){
                    isMonetaryAlreadyFound = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean createReportJSON(String path, int moduleVID){
        Path reportJsonPath = Paths.get(path + Utils.getSeparator(), Constants.reportJSON);
        String entryPointURL = ConfTemplateDAL.getEntryPointURL(moduleVID);
        
        if(entryPointURL==null){
            
            return false;
        }
        try (FileWriter writer = new FileWriter(reportJsonPath.toFile())) {
            writer.append(Constants.REPORTJSONCONTENTPARTBEGIN);
            writer.append(entryPointURL);
            writer.append(Constants.REPORTJSONCONTENTPARTEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
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
