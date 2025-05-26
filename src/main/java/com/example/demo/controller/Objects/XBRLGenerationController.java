/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.example.demo.Data.*;
import com.example.demo.controller.Objects.ActionPhases.DownloadAction;
import com.example.demo.controller.Objects.DAL.*;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.controller.Objects.Generation.OutXBRLGenerated;
import com.example.demo.controller.Objects.Import.*;
import com.example.demo.controller.Objects.Logs.GenerateLogDAL;

public class XBRLGenerationController implements Runnable {

    private List<InImportedTablesTemp> fileList;
    private String threadName;

    private ModuleVersion module;
    private String domain;
    private ConfEntities entity;
    private LocalDate referenceDate;

    private final Logger LOG = Logger.getLogger(XBRLGenerationController.class);


    //public XBRLGenerationController(ActiveUser user, String threadName, ModuleVersion module, String domain, ConfEntities entity, LocalDate referenceDate) {
    public XBRLGenerationController(String threadName, ModuleVersion module, String domain, ConfEntities entity, LocalDate referenceDate) {
        //this.user = user;
        this.threadName = threadName;
        this.module = module;
        this.domain = domain;
        this.entity = entity;
        this.referenceDate = referenceDate;
    }

    @Override
    public void run() {
        //xbrlGenerationMain();
    }

    public void xbrlGenerationMain(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity,IO io) {
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
            List<ConfAppConfigs> configs = Info.getInstance().refDataGet(Constants.AppConfigsAll);
                
            //path = Utils.getFinalPathOfOS(configs.stream().filter(x -> x.getKey().equals(Constants.XBRLGENERATEDPATH)).findFirst().get().getValue(),configs.stream().filter(x -> x.getKey().equals(Constants.WINDOWSDISK)).findFirst().get().getValue());

            path = Paths.get("XBRL_Lite","Run", "Reports", "XBRL_Generated").toString();
            
            folderName = getEntity().getLeiCode() + "." + getDomain() + "_PT_" + Utils.applyVersionString(getModule()) + "_" + getModule().getCode().replace("_", "") + "_" + getReferenceDate() + "_" + now.format(formater);
            //Create folder
            finalFolder = path + Utils.getSeparator() + folderName;
            directory = new File(finalFolder);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            /*generationIo = new IO(//new IOState(Constants.processoPending, new IOTypeState(Constants.tipoStatePending)),
                    Info.getInstance().getIOStateByID(Constants.processoPending),
                    getReferenceDate(),
                    getModule(),
                    domain,
                    getEntity(),
                    LocalDateTime.now(),
                    null,
                    Info.getInstance().getConfActionByID(Integer.valueOf(Constants.actionGeneration)),//new ConfAction(Constants.actionImport),
                    getUser().getUserId().toUpperCase(),
                    folderName,
                    threadName,
                    folderName
            );*/

            /*generationIo = new IO(//new IOState(Constants.processoPending, new IOTypeState(Constants.tipoStatePending)),
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
            );*/
            io.setIoState(Info.getInstance().getIOStateByID(Constants.processoPending));
            io.setReferenceDate(getReferenceDate());
            io.setModule(getModule());
            io.setDomain(domain);
            io.setEntity(getEntity());
            io.setInitTimestamp(LocalDateTime.now());
            io.setEndTimestamp(null);
            io.setAction(Info.getInstance().getConfActionByID(Integer.valueOf(Constants.actionGeneration)));
            io.setUserId("Generation");
            io.setFilename(folderName);
            io.setThreadFilename(threadName);
            io.setFilenameserver(folderName);

            Connection.persist(em, io);

            //OutXBRLGenerated outXBRLGenerated = new OutXBRLGenerated(user.getUserId(), folderName, getModule(), now, getEntity(), domain, getReferenceDate(), generationIo);
            OutXBRLGenerated outXBRLGenerated = new OutXBRLGenerated("ABC", folderName, getModule(), now, getEntity(), domain, getReferenceDate(), io);

            Connection.persist(em, outXBRLGenerated);
            GenerateLogDAL.createNewGenerationLog("Geracao Iniciada com sucesso", outXBRLGenerated.getIdXBRLGenerate());
            //Get Object from NULL
            boolean altGeneration = Info.getInstance().checkIfUsesAltGeneration(module.getModuleVID(),Constants.GENERATIONBASEDONCOLLUMN);
            List<InImportedTablesTemp> tempList = InImportedTablesDAL.getListOfImportedMaps(module, referenceDate, entity, domain, io);

            for (InImportedTablesTemp inImportedTablesTemp : tempList) {
                LOG.info("GetTableCode:" + inImportedTablesTemp.getTableVersion());
            }
            //Create Map
            Map<String, List<InImportedTablesTemp>> listOfTableGroupedByTheTableVID = tempList.stream()
                    .collect(Collectors.groupingBy(item -> item.getTableVersion().getCode()));
            Queue<Thread> threadList = new LinkedList<>();
            //HERE
            for (Map.Entry<String, List<InImportedTablesTemp>> entry : listOfTableGroupedByTheTableVID.entrySet()) {

                Thread t = new Thread(new XBRLGeneratorMap(finalFolder, entry.getValue(), io.getReferenceDate(), altGeneration));
                threadList.add(t);
                t.start();
            }
            createParametersCSV(finalFolder);
            Set<String> filteredMapWithoutEmpty = listOfTableGroupedByTheTableVID.entrySet().stream()
                    .filter(entry -> {
                        InImportedTablesTemp obj = entry.getValue().get(0);
                        return obj.getIoState().getIoStateId() != 12; //TODO remove HardCoded number
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
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
                io.setEndTimestamp(LocalDateTime.now());
                io.setIoState(Info.getInstance().getIOStateByID(Constants.processoCanceled));//new IOState(Constants.processoCanceled, new IOTypeState(Constants.tipoStateCanceled)));
                Connection.merge(io);    

            } else {
                //Add to the Log Generation Conclude sucesufully
                GenerateLogDAL.createNewGenerationLog("Geracao Concluída com sucesso", outXBRLGenerated.getIdXBRLGenerate());
                io.setEndTimestamp(LocalDateTime.now());
                io.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));//new IOState(Constants.processoOk, new IOTypeState(Constants.tipoStateOK)));
                Connection.merge(io);                
            }
            //OperationRunningDAL.deleteOperationRun(threadName);
            //remove Operation Table

        } catch (Exception e) {
            LOG.error("Erro na geracao:" + e.getMessage());
            e.printStackTrace();
            io.setEndTimestamp(LocalDateTime.now());
            io.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));//new IOState(Constants.processoNotOk, new IOTypeState(Constants.tipoStateNotOk)));
            Connection.merge(io); 
        }
        DownloadAction downloadAction = new DownloadAction();

        downloadAction.startDownload(finalFolder, io);
    }

    public void createFillingIndicatorCSV(String path, Set<String> maps) {
        List<TableVersionDPM> fillingIndicatorModuleList = TableVersionDAL.getAllFilesImported(getModule(), getReferenceDate());
        Path fillingCSVPath = Paths.get(path + Utils.getSeparator(), "FilingIndicators.csv");
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
            writer.append("templateID,reported");
            for (Map.Entry<String, Boolean> entry : mapNameChecker.entrySet()) {
                writer.append("\n");
                writer.append(entry.getKey());
                writer.append(",");
                writer.append((entry.getValue() ? "true" : "false"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createParametersCSV(String path) {
        Path paramCSVPath = Paths.get(path + Utils.getSeparator(), "parameters.csv");
        String currency = ConfAppConfigsDAL.getValueOfAppConfigurationKey("CURRENCY");
        String monetary = ConfAppConfigsDAL.getValueOfAppConfigurationKey("MONETARY");
        String percentage = ConfAppConfigsDAL.getValueOfAppConfigurationKey("PERCENTAGE");
        String decimal = ConfAppConfigsDAL.getValueOfAppConfigurationKey("DECIMAL");
        String integer = ConfAppConfigsDAL.getValueOfAppConfigurationKey("INTEGER");
        try (FileWriter writer = new FileWriter(paramCSVPath.toFile())) {
            writer.append("name,value\n");
            writer.append("entityID,");
            writer.append(getEntity().getLeiCode());
            writer.append(".");
            writer.append(domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase());
            writer.append("\n");
            writer.append("refPeriod,");
            writer.append(getReferenceDate().toString()); //TODO: Normalize date
            writer.append("\n");
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
            writer.append(decimal);
        } catch (IOException e) {
            //Something Wrong Happen
        }
    }

    /*public ActiveUser getUser() {
        return user;
    }

    public void setUser(ActiveUser user) {
        this.user = user;
    }*/

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
}
