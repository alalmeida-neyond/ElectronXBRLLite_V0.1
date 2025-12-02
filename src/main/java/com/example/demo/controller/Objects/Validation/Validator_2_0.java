package com.example.demo.controller.Objects.Validation;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.example.demo.DTOs.*;
import com.example.demo.Data.*;
import com.example.demo.Data.Access.*;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.Conf.*;
import com.example.demo.controller.Objects.Entities.DAL.*;
import com.example.demo.controller.Objects.Entities.DPMOrigin.*;
import com.example.demo.controller.Objects.Entities.Logs.LogOperationTemp;
import com.example.demo.controller.Objects.Extensions.RunnableExtension;
import com.example.demo.controller.Objects.Generation.XBRLGenerator;
import com.example.demo.controller.Objects.IO.IO;
import com.example.demo.controller.Objects.IO.IOState;
import com.example.demo.controller.Objects.Logs.*;
import com.example.demo.service.ProgressService;

import org.jboss.logging.Logger;

import com.example.demo.Resources.Utils;


public class Validator_2_0 extends RunnableExtension {
    
    private final Logger LOG = Logger.getLogger(Validator_2_0.class.getName());
    
    private ModuleVersion moduleVersion;
    private ConfEntities entity;
    private LocalDate refDate;
    private String domain;
    private Set<TableVersionDPM> tables;
    private ConnectionManager cm;

    private ProgressService progressService;

    private List<TableVersionDPM> importedTables;
    private List<Integer> selectedMapsToValidate;
    private List<IO> validateIOs;
    private Path generatedXBRL;

    
    public Validator_2_0(ModuleVersion moduleVersion, LocalDate refDate, ConfEntities entity, String domain, ProgressService progressService){
        this.moduleVersion = moduleVersion;
        this.entity = entity;
        this.refDate = refDate;
        this.domain = domain;
        this.progressService = progressService;
        this.cm = new ConnectionManager();
    }

    private Map<Integer, Map<Integer, List<ValNode>>> getNodes(int tableVId) {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList("SQL_Queries/XBRLArvore.sql", "OperationNodeMapping",
                    "moduleVId", String.valueOf(moduleVersion.getModuleVID()),
                    "refdate", refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "format",Constants.ISOBASEFORMAT8601,
                    "tableVId", String.valueOf(tableVId)
            );
        } catch (Exception e) {
            LOG.error("Erro na query getNodes: " + e.getMessage());
        } finally {
            Connection.close(jpa.getEm().em);
        }

        return mapNodesFromDatabase(results);
    }
    
    private Map<Integer, Map<Integer, List<ValNode>>> getNodesForPreconditions(){
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList("SQL_Queries/XBRLArvorePreconditions.sql", "OperationNodeMapping",
                    "moduleVId", String.valueOf(moduleVersion.getModuleVID()),
                    "refdate", refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "format",Constants.ISOBASEFORMAT8601
            );
        } catch (Exception e) {
            LOG.error("Erro na query getNodes: " + e.getMessage());
        } finally {
            Connection.close(jpa.getEm().em);
        }

        return mapNodesFromDatabase(results);
    }
    
    /**
     * Mapeamento dos resultados em Nós por nível.
     *
     * @param results Resultados da Query "XBRLArvore.sql"
     * @return Lista de nós mapeados por níves
     */
    private Map<Integer, Map<Integer, List<ValNode>>> mapNodesFromDatabase(List<Object[]> results) {
        Map<Integer, Map<Integer, List<ValNode>>> nodesFromDatabse = new HashMap<>();

        try {
            for (Object[] result : results) {
                OperationNode node = (OperationNode) result[0];
                Integer level = Integer.valueOf(result[1].toString());
                Integer operationVID = node.getOperationVersion().getOperationVID();
                
                ValNode nodeResult = new ValNode();
                nodeResult.setNode(node);
                nodeResult.setLevel(level);

                if (!nodesFromDatabse.containsKey(operationVID)) {
                    nodesFromDatabse.put(operationVID, new HashMap<>());
                }

                if (!nodesFromDatabse.get(operationVID).containsKey(level)) {
                    nodesFromDatabse.get(operationVID).put(level, new ArrayList<>());
                }

                nodesFromDatabse.get(operationVID).get(level).add(nodeResult);
            }
        } catch (Exception e) {
            LOG.error("Erro na mapNodesFromDatabase: " + e.getMessage());
        }

        return nodesFromDatabse;
    }

    private Map<Integer, Map<Integer, List<ValNode>>> gettingNodesForPrecondtionsAndCalculateTime(IO io){
        long initGetPreconditionsProcess = System.nanoTime();
        Map<Integer, Map<Integer, List<ValNode>>> nodesMappedByPreconditionVIdByLevel = getNodesForPreconditions();
        long endGetPreconditionsProcess = System.nanoTime();
        
        Connection.persist(this.cm, new LogOperationTemp("Obter Pré Condições | Duração: " + Utils.calculateTime(initGetPreconditionsProcess, endGetPreconditionsProcess) + " segundos", io.getIoId()));    
        
        return nodesMappedByPreconditionVIdByLevel;
    }
    
    private Map<Integer, Map<Integer, List<ValNode>>> gettingNodesForOperationsAndCalculateTime(int tableVId, String tableCode, IO io){
        long initGetOperationsProcess = System.nanoTime();
        Map<Integer, Map<Integer, List<ValNode>>> nodesMappedByOperationVIdByLevel = getNodes(tableVId);
        long endGetOperationsProcess = System.nanoTime();
        
        Connection.persist(this.cm, new LogOperationTemp("Obter Operações para o Mapa: " + tableCode + " | Duração: " + Utils.calculateTime(initGetOperationsProcess, endGetOperationsProcess) + " segundos", io.getIoId()));    
        
        return nodesMappedByOperationVIdByLevel;
    }



    public List<TableVersionDPM> getImportedMaps(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename, IO io){
        if(moduleVersion != null){
            importedTables = InImportedTablesDAL.getMapsToValidate(moduleVersion);
            selectedMapsToValidate = importedTables.stream().map(TableVersionDPM::getTableVID).collect(Collectors.toList());
        }else{
            importedTables = new ArrayList<>();
            selectedMapsToValidate = new ArrayList<>();
        }
        return importedTables;
    }

    public void startValidation(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename, IO io) {
        List<IO> operationsRunningFromIO = IODAL.getOperationRunningFromIO(moduleVersion, domain, entity, referenceDate.toString());
        if (!operationsRunningFromIO.isEmpty()) {
            LOG.info(Constants.concurrentOperations + " | " + Constants.concurrentOperationsDesc);
            return;
        }
        LOG.info("Buscar os mapas importados"); 
        importedTables = getImportedMaps(referenceDate, moduleVersion, domain, entity, filename, io);
        
        LOG.info("Colocar as tabelas a validar"); 
        List<TableVersionDPM> tablesToValidate = new ArrayList<>();
        for (TableVersionDPM importedTable : importedTables) {
            if(selectedMapsToValidate.contains(importedTable.getTableVID()))
                tablesToValidate.add(importedTable);
        }

        LOG.info("Ordernar as tabelas a validar pelo código do Table Version"); 
        Set<TableVersionDPM> sortedTables = tablesToValidate.stream().collect(Collectors.toCollection(() -> 
                                                    new TreeSet<>(Comparator.comparing(TableVersionDPM::getCode))
                                                ));
        /* 
        AQUIXXX  
        
        setTables(new HashSet<>());
        */
        setTables(sortedTables); 
        
        LOG.info("Validacao Iniciada | " + "Processo de validacao iniciada.");
        
        try {
            validateOperations(referenceDate, moduleVersion, domain.toUpperCase(), entity, filename, sortedTables, io);
        } catch (Exception e) {
            
        }
    }
    
    public String getEstadoForExcel(IOState ioState){
        String estado = null;
        
        if(ioState.getIoTypeStateId().getIoTypeStateId() == 3){
            estado = Constants.PENDENTE;
            return estado;
        }
        if(ioState.getIoTypeStateId().getIoTypeStateId() == 2){
            estado = Constants.NOTOK;
            return estado;
        }
        if(ioState.getIoTypeStateId().getIoTypeStateId() == 12){
            estado = Constants.OKComMapasVazios;
            return estado;
        }
        if(ioState.getIoTypeStateId().getIoTypeStateId() == 1 && ioState.getIoStateId() != 12 && ioState.getIoStateId() != 2){
            estado = Constants.OK;
            return estado;
        }
        if(ioState.getIoStateId() == 2){
            estado = Constants.OKMapasComErros;
            return estado;
        }
        return estado;
    }

    public List<TableVersionDPM> getImportedTables() {
        return importedTables;
    }

    public void setImportedTables(List<TableVersionDPM> importedTables) {
        this.importedTables = importedTables;
    }

    public List<IO> getValidateIOs() {
        return validateIOs;
    }

    public void setValidateIOs(List<IO> validateIOs) {
        this.validateIOs = validateIOs;
    }

    public List<Integer> getSelectedMapsToValidate() {
        return selectedMapsToValidate;
    }

    public void setSelectedMapsToValidate(List<Integer> selectedMapsToValidate) {
        this.selectedMapsToValidate = selectedMapsToValidate;
    }
    

    

    private Map<Integer, List<ValResult>> getResultsByNode(int operationVID, IO io) {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();

        try {
            results = jpa.getMappedFileQueryResultList("SQL_Queries/GetValuesUpdate.sql", "ValuesForOperationMapping",
                    "operationVId", String.valueOf(operationVID),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "actionId", Constants.actionImport,
                    "domain", io.getDomain(),
                    "entityId", String.valueOf(io.getEntity().getEntityID()),                   
                    "refdate", io.getReferenceDate().format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "format",Constants.ISOBASEFORMAT8601,
                    "desagregationTypeFixed", String.valueOf(Constants.DESAGREGATIONCODEFIXEDTYPE),
                    "directionZ", String.valueOf(Constants.SHEETCOORDINATE),
                    "dataTypeDate", String.valueOf(Constants.DATATYPEDATE),
                    "refPeriodString", String.valueOf(Constants.REFPERIOD),
                    "dataTypeEnumeration", String.valueOf(Constants.DATATYPEENUMERATION),
                    "referenceRow", String.valueOf(Constants.PROPERTYROW),
                    "referenceColumn", String.valueOf(Constants.PROPERTYCOLUMN),
                    "referenceSheet", String.valueOf(Constants.PROPERTYSHEET),
                    "stateOk", String.valueOf(Constants.processoOk),
                    "desagregationCodeType", String.valueOf(Constants.DESAGREGATIONCODETYPE)
            );
        } catch (Exception e) {
            LOG.error("Erro na query getResultsByNode: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connection.close(jpa.getEm().em);
        }

        return OperationsUtils.mapResultsFromDatabase(results, refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS));
    }
    
    private Map<Integer, List<ValResult>> getResultsByNodeForPreconditions(int precondtionVId, IO io) {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList("SQL_Queries/GetValuesForPreconditionsUpdate.sql", "ValuesForOperationMapping",
                    "preconditionVId", String.valueOf(precondtionVId),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "actionId", String.valueOf(Constants.actionImport),
                    "refdate", io.getReferenceDate().format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "format",Constants.ISOBASEFORMAT8601,
                    "domain", io.getDomain(),
                    "entityId", String.valueOf(io.getEntity().getEntityID()),
                    "processoOkDeleted", Constants.processoOkDeleted
            );
        } catch (Exception e) {
            LOG.error("Erro na query getResultsByNodeForPreconditions: " + e.getMessage());
        } finally {
            Connection.close(jpa.getEm().em);
        }

        return OperationsUtils.mapResultsFromDatabase(results, refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS));
    }
    
    public void validateOperations(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename, Set<TableVersionDPM> sortedTables,IO ioImport) {
        OutValidationResult commonDatapointValidationResult = null;

        Map<Integer, ValResult> resultPerPrecondition = new HashMap<>();
        Map<Integer, OutValidationResult> operationsResultsIds = new HashMap<>();
        
        IO ioValidation = null;
        
        // temp
        // List<Integer> operationVIDs = Arrays.asList(7472);

        try {
            long initAllProcess = System.nanoTime();
            ioValidation = new IO(
                    Info.getInstance().getIOStateByID(Constants.processoPending),
                    refDate, moduleVersion, domain, entity, 
                    LocalDateTime.now(), Info.getInstance().getConfActionByID(Integer.valueOf(Constants.actionValidation))
                    , "Validation");

            ioValidation.setFilename(ioImport.getFilename());
            ioValidation.setFilenameserver(ioImport.getFilenameserver());
            ioValidation.setThreadFilename(ioImport.getThreadFilename());
            Connection.persist(cm, ioValidation);

            
            //Obtencao dos nós da árvore por operacao
            Map<Integer, Map<Integer, List<ValNode>>> nodesMappedByPreconditionVIdByLevel = gettingNodesForPrecondtionsAndCalculateTime(ioValidation);
            
            int completedTables = 1;
            progressService.setValidationProgress(completedTables, Integer.valueOf(getTables().size()) + 1);
                    
            for (TableVersionDPM table : getTables()) {
                // temp
                /* List<Integer> tableVIDs = Arrays.asList(2328, 2331, 5465, 5469, 6092, 6110, 6292, 6295);
                if(!tableVIDs.contains(table.getTableVID())){
                    continue;
                } */
                TableDPM auxTable = TableVersionDAL.getTableDPM(table.getTableVID());
                table.setTable(auxTable); 
                
                long initPerMap = System.nanoTime();
                Integer tableVId = table.getTableVID();
                                
                
                OutValidationTable outValTable = new OutValidationTable(table, ioValidation,Info.getInstance().getIOStateByID(Constants.processoPending));
                Connection.persist(cm, outValTable);
                
                // temp
                /* getResultsByNode(7445, ioImport);
                if (true)return; */

                Map<Integer, Map<Integer, List<ValNode>>> nodesMappedByOperationVIdByLevel = gettingNodesForOperationsAndCalculateTime(tableVId, table.getCode(), ioValidation);
                
                
                
                List<CommonDatapointValidationDTO> possibleDatapointsConflicts = InImportedTablesDAL.getPossibleDataPointsConflicts(table, refDate, domain, entity, ioImport);
                commonDatapointValidationResult = validateCommonDatapoints(possibleDatapointsConflicts, cm);
                if (commonDatapointValidationResult != null) {
                    OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, commonDatapointValidationResult);
                    Connection.persist(cm, outValTableResult);
                }

                if (nodesMappedByOperationVIdByLevel.isEmpty() && possibleDatapointsConflicts.isEmpty()) {
                    outValTable.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));
                    progressService.setValidationProgress(completedTables++, Integer.valueOf(getTables().size()) + 1);
                    continue;
                }

                for (Integer operationVId : nodesMappedByOperationVIdByLevel.keySet()) {
                    // temp
                    /* if(!operationVIDs.contains(operationVId)){
                        continue;
                    } */
                    

                    //Verifica se a regra já foi validada
                    if (operationsResultsIds.containsKey(operationVId)) {
                        OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, operationsResultsIds.get(operationVId));
                        Connection.persist(cm, outValTableResult);
                    } else {
                        //Cria o Out_ValidationResult
                        OperationVersion operation = nodesMappedByOperationVIdByLevel.get(operationVId).get(1).get(0).getOperationVersion();
                        OutValidationResult outValResult = new OutValidationResult(operation, Info.getInstance().getIOStateByID(Constants.processoPending));//new IOState(Constants.processoPending, new IOTypeState(Constants.tipoStatePending)));
                    
                        //Valida a precondicao caso ainda não tenha sido validada
                        Integer preConditionVId = nodesMappedByOperationVIdByLevel.get(operationVId).get(1).get(0).getPreconditonOperationVId();
                        
                        if (preConditionVId != null && !resultPerPrecondition.containsKey(preConditionVId)) {
                            Map<Integer, List<ValNode>> nodesMappedByLevel = nodesMappedByPreconditionVIdByLevel.get(preConditionVId);
                            validatePrecondition(preConditionVId, nodesMappedByLevel, resultPerPrecondition, ioImport);
                        }

                        ValResult preConditionResult = (preConditionVId != null) ? resultPerPrecondition.get(preConditionVId) : null;

                        //caso a precondicao
                        if(preConditionVId != null && (preConditionResult.valueIsNull() || !Boolean.parseBoolean(preConditionResult.getRawValue()))){
                            outValResult.setIoState(Info.getInstance().getIOStateByID(Constants.RULEDONOTRUNPREREQUISITE.getKey()));
                            
                            operationsResultsIds.put(operationVId, outValResult);
                            Connection.persist(cm, outValResult);

                            OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, operationsResultsIds.get(operationVId));
                            Connection.persist(cm, outValTableResult);
                        } else if (preConditionVId == null || Boolean.parseBoolean(preConditionResult.getRawValue())) {
                            long initPerRule = System.nanoTime();
                            

                            long initGetValuesProcess = System.nanoTime();
                            Map<Integer, List<ValResult>> resultsMappedByNode = getResultsByNode(operationVId, ioImport);
                            long endGetValuesProcess = System.nanoTime();

                            String durationGetValues = Utils.calculateTime(initGetValuesProcess, endGetValuesProcess);
                            Map<Integer, List<ValNode>> nodesMappedByLevel = nodesMappedByOperationVIdByLevel.get(operationVId);

                            List<ValResult> results = validateOperation(nodesMappedByLevel, resultsMappedByNode, operationVId);

                            resultsMappedByNode.clear();
                            nodesMappedByLevel.clear();

                            if (results != null && !results.isEmpty()) {
                                List<OutValidationResultDetails> resultDetails = OutValidationResultDetails.createResultDetails(results, outValResult);
                                
                                results.clear();
                                
                                outValResult.setIoState(resultDetails);

                                operationsResultsIds.put(operationVId, outValResult);
                                Connection.persist(cm, outValResult);

                                OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, operationsResultsIds.get(operationVId));
                                Connection.persist(cm, outValTableResult);

                                Connection.persistList(cm, resultDetails);
                            } else {
                                outValResult.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));
                                
                                operationsResultsIds.put(operationVId, outValResult);
                                Connection.persist(cm, outValResult);

                                OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, operationsResultsIds.get(operationVId));
                                Connection.persist(cm, outValTableResult);
                            }

                            long endPerRule = System.nanoTime();
                            Connection.persist(cm, new LogOperationTemp("Operação: " + operation.getOperationCode() + " | Obter Valores: " + durationGetValues + "s | Validação: " + Utils.calculateTime(initPerRule, endPerRule) + "s", ioValidation.getIoId()));
                        }
                    }
                }

                long endPerMap = System.nanoTime();
                Connection.persist(cm, new LogOperationTemp("Término da validação do mapa: " + table.getCode() + " | Duração: " + Utils.calculateTime(initPerMap, endPerMap) + " segundos", ioValidation.getIoId()));
                
                if(operationsResultsIds.isEmpty() && possibleDatapointsConflicts.isEmpty()){
                    outValTable.setIoState(Info.getInstance().getIOStateByID(Constants.processoOkEmpty));
                } else {
                    outValTable.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));
                }
                Connection.merge(cm, outValTable);
                
                progressService.setValidationProgress(completedTables++, Integer.valueOf(getTables().size()) + 1);
            }
            
            long endAllProcess = System.nanoTime();
            Connection.persist(cm, new LogOperationTemp("Término da validação | Duração: " + Utils.calculateTime(initAllProcess, endAllProcess) + " segundos", ioValidation.getIoId()));


            progressService.setValidationProgress(getTables().size()+1, Integer.valueOf(getTables().size()) + 1);

                
            ioValidation.setEndTimestamp(LocalDateTime.now());
            ioValidation.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));
            Connection.merge(cm, ioValidation);
            
            int persistResult = persistIntoValidationsDashboard(cm, ioImport);
            if(persistResult != 1){
                LogValidationProcess logValProcessErrorInsertDashboard = new LogValidationProcess(ioValidation, "Ocorreu um erro na insercao dos dados para consulta no Dashboard de Validacoes.", LocalDateTime.now());
                LOG.error("Ocorreu um erro na insercao dos dados para consulta no Dashboard de Validacoes.");
                Connection.persist(cm, logValProcessErrorInsertDashboard); 
                ioValidation.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));
                Connection.merge(cm, ioValidation);
            }

            progressService.setValidationProgress(1, 1);
            
            LOG.info("Generation Start");
            XBRLGenerator generationAction = new XBRLGenerator(progressService,referenceDate, moduleVersion, domain.toUpperCase(), entity);

            generationAction.startGeneration(referenceDate, moduleVersion, domain.toUpperCase(), entity, filename, ioImport, ioValidation);
            setGeneratedXBRL(generationAction.getGeneratedXBRL());
        } catch (Exception e) {
            LOG.error("Ocorreu um erro no processo de validacao: " + e.getMessage());
            e.printStackTrace();

            ioValidation.setEndTimestamp(LocalDateTime.now());
            ioValidation.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));
            Connection.merge(cm, ioValidation);
        }
    }

    private List<ValResult> validateOperation(Map<Integer, List<ValNode>> nodesMappedByLevel, Map<Integer, List<ValResult>> resultsMappedByNode, Integer operationVId) {
        Map<Integer, List<ValNode>> nodesMappedByParent = new HashMap<>();
        boolean hasError = false;

        try {
            if (!nodesMappedByLevel.isEmpty() && !resultsMappedByNode.isEmpty()) {
                nodesMappedByParent = prepareOperation(resultsMappedByNode, nodesMappedByLevel);

                List<Integer> levels = nodesMappedByLevel.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

                //Executa a árvore de baixo para cima
                treeLoop:
                for (Integer level : levels) {
                    for (ValNode node : nodesMappedByLevel.get(level)) {
                        if (node.getNode().getOperator() != null) {
                            List<ValNode> childs = nodesMappedByParent.get(node.getNode().getNodeID());
                            Boolean valid = ValidationOperators.evaluate(node, childs, String.valueOf(entity.getEntityID()), getDomain());
                            if (valid == null || valid == false) {
                                hasError = true;
                                break treeLoop;
                            }
                        }
                    }
                }

                if (!hasError) {
                    return nodesMappedByLevel.get(1).get(Constants.FIRSTRESULT).getResults();
                } else {
                    return null;
                }
            }
        } catch (AssertionError | Exception e) {
            LOG.error("Erro na validacao da operacao" + operationVId + ": " + e.getMessage());
            e.printStackTrace();

        }

        return null;
    }

    private Map<Integer, List<ValNode>> prepareOperation(Map<Integer, List<ValResult>> resultsMappedByNode, Map<Integer, List<ValNode>> nodesMappedByLevel) {
        Map<Integer, List<ValNode>> nodesMappedByParent = new HashMap<>();

        try {
            if (nodesMappedByLevel != null && !nodesMappedByLevel.isEmpty() && resultsMappedByNode != null && !resultsMappedByNode.isEmpty()) {
                //Associacao dos valores aos respetivos nós folhas
                for (Integer key : nodesMappedByLevel.keySet()) {
                    for (ValNode node : nodesMappedByLevel.get(key)) {
                        List<ValResult> results = resultsMappedByNode.get(node.getNode().getNodeID());
                        node.setResults(results);

                        if (node.getNode().getParentNode() != null) {
                            Integer parentID = node.getNode().getParentNode().getNodeID();
                            nodesMappedByParent.computeIfAbsent(parentID, k -> new ArrayList<>()).add(node);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Erro no prepareOperation: " + e.getMessage());
        }
        return nodesMappedByParent;
    }

    
    private void validatePrecondition(Integer preConditionVId, Map<Integer, List<ValNode>> nodesMappedByLevel, Map<Integer, ValResult> resultPerPrecondition, IO io) {
        //Obtencao dos valores importados nos nós

        //obtém os dados para validar
        Map<Integer, List<ValResult>> resultsMappedByNode = getResultsByNodeForPreconditions(preConditionVId, io);

        //valida a operacao
        List<ValResult> results = validateOperation(nodesMappedByLevel, resultsMappedByNode, preConditionVId);

        if (results != null && !results.isEmpty()) {
            resultPerPrecondition.put(preConditionVId, results.get(Constants.FIRSTRESULT));
        }

    }

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public ConfEntities getEntity() {
        return entity;
    }

    public void setEntity(ConfEntities entity) {
        this.entity = entity;
    }

    public LocalDate getRefDate() {
        return refDate;
    }

    public void setRefDate(LocalDate refDate) {
        this.refDate = refDate;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Set<TableVersionDPM> getTables() {
        return tables;
    }

    public void setTables(Set<TableVersionDPM> tables) {
        this.tables = tables;
    }

    public Path getGeneratedXBRL()
    {
        return generatedXBRL;
    }

    public void setGeneratedXBRL(Path generatedXBRL)
    {
        this.generatedXBRL = generatedXBRL;
    }

    @Override
    public void run() {
        try {
            //validateOperations();
        } catch (Exception e) {
            LOG.error("Erro. Falha ao lancar thread de validacao: " + e.getMessage());
        }
    }

    private OutValidationResult validateCommonDatapoints(List<CommonDatapointValidationDTO> possibleDatapointsConflicts, ConnectionManager em) {
        if (!possibleDatapointsConflicts.isEmpty()) {
            //create the OutValidationResult
            OutValidationResult commonDatapointValidationResult = new OutValidationResult(null, Info.getInstance().getIOStateByID(Constants.processoPending));
            Connection.persist(em,commonDatapointValidationResult);
            for(CommonDatapointValidationDTO validation : possibleDatapointsConflicts){
                //insert the Results
                OutValidationResultDetails resultDetails = new OutValidationResultDetails();
                resultDetails.setIoState(Info.getInstance().getIOStateByID(Constants.RULENOTOK.getKey()));
                resultDetails.setUsedMargin(false);
                resultDetails.setDifference(null);
                resultDetails.setTimeStampCreated(LocalDateTime.now());
                resultDetails.setExpression(validation.getDetails());
                resultDetails.setDomain(validation.getDomain());
                resultDetails.setValidationResult(commonDatapointValidationResult);
                Connection.persist(em, resultDetails);
            }
            commonDatapointValidationResult.setIoState(Info.getInstance().getIOStateByID(Constants.RULENOTOK.getKey()));              
            Connection.merge(em,commonDatapointValidationResult);
            return commonDatapointValidationResult;
        }
        return null;
    }

    private int persistIntoValidationsDashboard(ConnectionManager cm, IO io) {
        int result = -1;
        JPA<Object[]> jpa = new JPA<>(cm, Object[].class);
 
        try {
            result = jpa.executeFileQuery("SQL_Queries/InsertIntoValidationsDashboard.sql",
                    "referenceDate", this.refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "ioId", String.valueOf(io.getIoId()),
                    "processoOk", String.valueOf(Constants.processoOk),
                    "stateRuleOk", String.valueOf(Constants.RULEOK.getKey()),
                    "stateRuleDNRR",String.valueOf(Constants.RULEDONOTRUN.getKey()),
                    "stateRuleDNRRPrequisite", String.valueOf(Constants.RULEDONOTRUNPREREQUISITE.getKey()),
                    "stateRuleNotOk", String.valueOf(Constants.RULENOTOK.getKey()),
                    "stateRuleOkWithNotOk", String.valueOf(Constants.RULEOKWITHNOTOK.getKey()),
                    "warningSeverity", Constants.SEVERITYWARNING,
                    "errorSeverity", Constants.SEVERITYERROR,
                    "stateProcessNotOk", String.valueOf(Constants.processoNotOk),
                    "processOkDeleted", String.valueOf(Constants.processoOkDeleted),
                    "format",Constants.DATEFORMATISO8601STRING
                );
        } catch (Exception e) {
            LOG.error("Ocorreu um erro ao realizar ao inserir os dados no Dashboard de Validacoes: " + e.getMessage());
            result = -1;
        }
        
        return result;
    }

}
