/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.DTOs.*;
import com.example.demo.Data.*;
import com.example.demo.Data.Access.*;
import com.example.demo.controller.Objects.ActionPhases.GenerationAction;
import com.example.demo.controller.Objects.DAL.*;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.controller.Objects.Logs.*;

import org.jboss.logging.Logger;


public class Validator_2_0 implements Runnable {
    
    private final Logger LOG = Logger.getLogger(Validator_2_0.class.getName());
    
    private ModuleVersion moduleVersion;
    private ConfEntities entity;
    private LocalDate refDate;
    private String domain;
    private String userID;
    private Set<TableVersionDPM> tables;
    
    //public Validator_2_0(ModuleVersion moduleVersion, LocalDate refDate, ConfEntities entity, String domain, String userID, Set<TableVersionDPM> tables){
    public Validator_2_0(ModuleVersion moduleVersion, LocalDate refDate, ConfEntities entity, String domain, Set<TableVersionDPM> tables){
        this.moduleVersion = moduleVersion;
        this.entity = entity;
        this.refDate = refDate;
        this.domain = domain;
        //this.userID = userID;
        this.tables = tables;
    }

    private Map<Integer, Map<Integer, List<ValNode>>> getNodes(int tableVId) {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList("XBRLArvore.sql", "OperationNodeMapping",
                    "moduleVId", String.valueOf(moduleVersion.getModuleVID()),
                    "refdate", refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "format",Constants.ISOBASEFORMAT8601SQLite,
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
            results = jpa.getMappedFileQueryResultList("XBRLArvorePreconditions.sql", "OperationNodeMapping",
                    "moduleVId", String.valueOf(moduleVersion.getModuleVID()),
                    "refdate", refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "format",Constants.ISOBASEFORMAT8601SQLite
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

    private Map<Integer, List<ValResult>> getResultsByNode(int operationVID, IO io) {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();

        try {
            results = jpa.getMappedFileQueryResultList("GetValuesUpdate.sql", "ValuesForOperationMapping",
                    "operationVId", String.valueOf(operationVID),
                    "ioId", io.getIoId(),
                    "refdate", refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "format",Constants.ISOBASEFORMAT8601SQLite,
                    "desagregationTypeFixed", String.valueOf(Constants.DESAGREGATIONCODEFIXEDTYPE),
                    "directionZ", String.valueOf(Constants.SheetCoordinate),
                    "dataTypeDate", String.valueOf(Constants.DATE),
                    "refPeriodString", String.valueOf(Constants.REFPERIOD),
                    "dataTypeEnumeration", String.valueOf(Constants.ENUMERATION),
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
            results = jpa.getMappedFileQueryResultList("GetValuesForPreconditionsUpdate.sql", "ValuesForOperationMapping",
                    "preconditionVId", String.valueOf(precondtionVId),
                    "ioId", io.getIoId());
        } catch (Exception e) {
            LOG.error("Erro na query getResultsByNodeForPreconditions: " + e.getMessage());
        } finally {
            Connection.close(jpa.getEm().em);
        }

        return OperationsUtils.mapResultsFromDatabase(results, refDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS));
    }
    
    public void validateOperations(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename, IO ioImport) {
        boolean hasErrors = false;
        OutValidationResult commonDatapointValidationResult = null;

        Map<Integer, ValResult> resultPerPrecondition = new HashMap<>();
        Map<Integer, OutValidationResult> operationsResultsIds = new HashMap<>();
        
        ConnectionManager em = null;
        IO ioValidation = null;
        
        //List<Integer> operationVIDs = Arrays.asList(10684);

        try {
            em = new ConnectionManager();
            long initAllProcess = System.nanoTime();
            ioValidation = new IO(//new IOState(Constants.processoPending, new IOTypeState(Constants.tipoStatePending)),
                    Info.getInstance().getIOStateByID(Constants.processoPending),
                    refDate, moduleVersion, domain, entity, 
                    LocalDateTime.now(), Info.getInstance().getConfActionByID(Integer.valueOf(Constants.actionValidation))
                    , "Validation");

            ioValidation.setFilename(ioImport.getFilename());
            ioValidation.setFilenameserver(ioImport.getFilenameserver());
            ioValidation.setThreadFilename(ioImport.getThreadFilename());
            Connection.persist(em, ioValidation);

            LOG.info("Processo de validacao iniciado: " + LocalDateTime.now());

            LogValidationProcess logValProcessInit = new LogValidationProcess(ioValidation, "Processo de validacao iniciado.", LocalDateTime.now());
            Connection.persist(em, logValProcessInit);
            
            //Obtencao dos nós da árvore por operacao
            Map<Integer, Map<Integer, List<ValNode>>> nodesMappedByPreconditionVIdByLevel = getNodesForPreconditions();
            
                    
            for (TableVersionDPM table : getTables()) {
                Integer tableVId = table.getTableVID();
                                
                long initPerMap = System.nanoTime();
                LOG.info("Comeco da avaliacao do mapa: " + table.getCode());
                
                OutValidationTable outValTable = new OutValidationTable(table, ioValidation,Info.getInstance().getIOStateByID(Constants.processoPending));
                Connection.persist(em, outValTable);
                
                Map<Integer, Map<Integer, List<ValNode>>> nodesMappedByOperationVIdByLevel = getNodes(tableVId);
                
                List<CommonDatapointValidationDTO> possibleDatapointsConflicts = InImportedTablesDAL.getPossibleDataPointsConflicts(table, refDate, domain, entity, ioImport);
                commonDatapointValidationResult = validateCommonDatapoints(possibleDatapointsConflicts, em);
                if (commonDatapointValidationResult != null) {
                    OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, commonDatapointValidationResult);
                    Connection.persist(em, outValTableResult);
                }

                if (nodesMappedByOperationVIdByLevel.isEmpty() && possibleDatapointsConflicts.isEmpty()) {
                    LogValidationProcess logValProcessMapEnd = new LogValidationProcess(ioValidation, "Validacao - " + table.getCode() + " - Concluido | Nao encontrou operacoes para este mapa.", LocalDateTime.now());
                    Connection.persist(em, logValProcessMapEnd);
                    
                    outValTable.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));//new IOState(Constants.processoOk, new IOTypeState(Constants.tipoStateOK)));
                    continue;
                }

                for (Integer operationVId : nodesMappedByOperationVIdByLevel.keySet()) {
                    /* temp
                        if(!operationVIDs.contains(operationVId)){
                            continue;
                        }
                    */

                    //Verifica se a regra já foi validada
                    if (operationsResultsIds.containsKey(operationVId)) {
                        OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, operationsResultsIds.get(operationVId));
                        Connection.persist(em, outValTableResult);
                    } else {
                        //Cria o Out_ValidationResult
                        OperationVersion operation = nodesMappedByOperationVIdByLevel.get(operationVId).get(1).get(0).getOperationVersion();
                        OutValidationResult outValResult = new OutValidationResult(operation, Info.getInstance().getIOStateByID(Constants.processoPending));//new IOState(Constants.processoPending, new IOTypeState(Constants.tipoStatePending)));
                    
                        //Valida a precondicao caso ainda não tenha sido validada
                        Integer preConditionVId = nodesMappedByOperationVIdByLevel.get(operationVId).get(1).get(0).getPreconditonOperationVId();
                        if (!resultPerPrecondition.containsKey(preConditionVId)) {
                            Map<Integer, List<ValNode>> nodesMappedByLevel = nodesMappedByPreconditionVIdByLevel.get(preConditionVId);
                            validatePrecondition(preConditionVId, nodesMappedByLevel, resultPerPrecondition, ioImport);
                        }

                        ValResult preConditionResult = resultPerPrecondition.get(preConditionVId);

                        //caso a precondicao
                        if(preConditionResult.valueIsNull() || !Boolean.parseBoolean(preConditionResult.getRawValue())){
                            outValResult.setIoState(Info.getInstance().getIOStateByID(Constants.RULEDONOTRUNPREREQUISITE.getKey()));
                            
                            operationsResultsIds.put(operationVId, outValResult);
                            Connection.persist(em, outValResult);

                            OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, operationsResultsIds.get(operationVId));
                            Connection.persist(em, outValTableResult);
                        } else if (Boolean.parseBoolean(preConditionResult.getRawValue())) {
                            //Obtencao dos valores importados nos nós
                            LOG.info("Avaliacao da regra: " + operationVId);

                            //obtém os dados para validar
                            Map<Integer, List<ValResult>> resultsMappedByNode = getResultsByNode(operationVId, ioImport);
                            Map<Integer, List<ValNode>> nodesMappedByLevel = nodesMappedByOperationVIdByLevel.get(operationVId);

                            //valida a operacao
                            List<ValResult> results = validateOperation(nodesMappedByLevel, resultsMappedByNode, operationVId);

                            //Estes clears sao atualizacoes do XBRL 2.0
                            resultsMappedByNode.clear();
                            nodesMappedByLevel.clear();

                            if (results != null && !results.isEmpty()) {
                                List<OutValidationResultDetails> resultDetails = OutValidationResultDetails.createResultDetails(results, outValResult);
                                
                                //Este clear e atualizacao do XBRL 2.0
                                results.clear();
                                
                                outValResult.setIoState(resultDetails);

                                operationsResultsIds.put(operationVId, outValResult);
                                Connection.persist(em, outValResult);

                                OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, operationsResultsIds.get(operationVId));
                                Connection.persist(em, outValTableResult);

                                Connection.persistList(em, resultDetails);
                            } else {
                                //ALGO CORREU MAL
                                outValResult.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));//new IOState(Constants.processoNotOk, new IOTypeState(Constants.tipoStateNotOk)));
                                
                                operationsResultsIds.put(operationVId, outValResult);
                                Connection.persist(em, outValResult);

                                OutValidationTableResult outValTableResult = new OutValidationTableResult(outValTable, operationsResultsIds.get(operationVId));
                                Connection.persist(em, outValTableResult);

                                insertOperationLogs(ioValidation);
                                Info.getInstance().getValidationsLogs().clear();
                            }

                            LOG.info("Fim da avaliacao da regra: " + operationVId);
                        }
                    }
                }

                long endPerMap = System.nanoTime();
                float durationPerMap = ((float) (endPerMap - initPerMap) / 1000000000);
                String durationFormatted = String.format("%.2f", durationPerMap);
                LOG.info("Término da avaliacao do mapa: " + table.getCode() + " | Duracao: " + durationFormatted + " segundos");
                
                if(operationsResultsIds.isEmpty()){
                    outValTable.setIoState(Info.getInstance().getIOStateByID(Constants.processoOkEmpty));//new IOState(Constants.processoOkEmpty, new IOTypeState(Constants.tipoStateOK)));
                } else {
                    outValTable.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));//new IOState(Constants.processoOk, new IOTypeState(Constants.tipoStateOK)));
                }
                Connection.merge(em, outValTable);

                LogValidationProcess logValProcessMapEnd = new LogValidationProcess(ioValidation, "Validacao - " + table.getCode() + " - Concluída", LocalDateTime.now());
                Connection.persist(em, logValProcessMapEnd);
            }

            //if(commonDatapointValidationResult != null){
            //    commonDatapointValidationResult.setIoState(new IOState(Constants.RULENOTOK.getKey(), new IOTypeState(Constants.RULENOTOK.getValue())));
            //    Connection.merge(em, commonDatapointValidationResult);
            //}
            
            long endAllProcess = System.nanoTime();
            float durationAllProcess = ((float) (endAllProcess - initAllProcess) / 1000000000);
            String durationFormatted = String.format("%.2f", durationAllProcess);

            LOG.info("Termino da validacao | Duracao: " + durationFormatted + " segundos");
            
            LogValidationProcess logValProcessEnd = new LogValidationProcess(ioValidation, "Processo de validacao foi concluido.", LocalDateTime.now());
            Connection.persist(em, logValProcessEnd); 
                
            ioValidation.setEndTimestamp(LocalDateTime.now());
            ioValidation.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));//new IOState(Constants.processoOk, new IOTypeState(Constants.tipoStateOK)));
            Connection.merge(em, ioValidation);
            
            int persistResult = persistIntoValidationsDashboard(em, ioImport);
            if(persistResult != 1){
                LogValidationProcess logValProcessErrorInsertDashboard = new LogValidationProcess(ioValidation, "Ocorreu um erro na insercao dos dados para consulta no Dashboard de Validacoes.", LocalDateTime.now());
                LOG.error("Ocorreu um erro na insercao dos dados para consulta no Dashboard de Validacoes.");
                Connection.persist(em, logValProcessErrorInsertDashboard); 
                ioValidation.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));
                Connection.merge(em, ioValidation);
            }
            else{
                LOG.info("Validacao com sucesso");
            }
            
            LOG.info("Generation Start");
            GenerationAction generationAction = new GenerationAction();

            generationAction.startGeneration(referenceDate, moduleVersion, domain.toUpperCase(), entity, filename, ioImport, ioValidation);
        } catch (Exception e) {
            LOG.error("Ocorreu um erro no processo de validacao: " + e.getMessage());
            e.printStackTrace();
            
            LogValidationProcess logValProcessEnd = new LogValidationProcess(ioValidation, "Processo de validacao ocorreu com erros inesperados.", LocalDateTime.now());
            Connection.persist(em, logValProcessEnd);

            ioValidation.setEndTimestamp(LocalDateTime.now());
            ioValidation.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));//new IOState(Constants.processoNotOk, new IOTypeState(Constants.tipoStateNotOk)));
            Connection.merge(em, ioValidation);
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
                                //Utils.addLogOfOperations(operationVId, node.getNode().getNodeID(), "Aconteceu algo de errado na operacao", null, null, "Erro");
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

    private static Boolean insertOperationLogs(IO validationIo) {
        JPA<LogValidationProcess> jpa = new JPA<>(LogValidationProcess.class);
        List<LogValidationProcess> logsList = Info.getInstance().getValidationsLogs();
        boolean result = false;

        try {
            if (!logsList.isEmpty()) {
                for (LogValidationProcess log : logsList) {
                    log.setIo(validationIo);
                }
                result = Connection.persistList(jpa.getEm(), logsList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
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

    /*private void insertResultsLogs(Integer operationVId, Integer nodeId, List<ValResult> results, int code) {
        if(results != null && !results.isEmpty()){
            for (ValResult result : results) {
                StringBuilder sb = new StringBuilder();
                sb.append((result.getRawValue() != null) ? result.getRawValue() : "null");
                sb.append(" | Expressão: ").append(result.getExpression());
                
                Utils.addLogOfOperations(operationVId, nodeId, 
                                            sb.toString(), 
                                            null, (result.getKey() != null) ? result.getKey().toString() : null, 
                                            (code == 1) ? "Resultado" : (code == 2) ? "PreCondicao" : "null");
            }
        }
    }*/
    private void validatePrecondition(Integer preConditionVId, Map<Integer, List<ValNode>> nodesMappedByLevel, Map<Integer, ValResult> resultPerPrecondition, IO io) {
        //Obtencao dos valores importados nos nós
        LOG.info("Avaliacao da precondicao: " + preConditionVId);

        //obtém os dados para validar
        Map<Integer, List<ValResult>> resultsMappedByNode = getResultsByNodeForPreconditions(preConditionVId, io);

        //valida a operacao
        List<ValResult> results = validateOperation(nodesMappedByLevel, resultsMappedByNode, preConditionVId);

        if (results != null && !results.isEmpty()) {
            //Integer nodeId = nodesMappedByLevel.get(1).get(Constants.FIRSTRESULT).getNode().getNodeID();
            //insertResultsLogs(preConditionVId, nodeId, results, 2);
            resultPerPrecondition.put(preConditionVId, results.get(Constants.FIRSTRESULT));
        }

        LOG.info("Fim da avaliacao da précondicao: " + preConditionVId);

        //insertOperationLogs();
        //Info.getInstance().clearLogOperationsList();
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
                resultDetails.setIoState(Info.getInstance().getIOStateByID(Constants.RULENOTOK.getKey()));//new IOState(Constants.RULENOTOK.getKey(), new IOTypeState(Constants.RULENOTOK.getValue())));
                resultDetails.setUsedMargin(false);
                resultDetails.setDifference(null);
                resultDetails.setTimeStampCreated(LocalDateTime.now());
                resultDetails.setExpression(validation.getDetails());
                resultDetails.setDomain(validation.getDomain());
                resultDetails.setValidationResult(commonDatapointValidationResult);
                Connection.persist(em, resultDetails);
            }
            commonDatapointValidationResult.setIoState(Info.getInstance().getIOStateByID(Constants.RULENOTOK.getKey()));//new IOState(Constants.RULENOTOK.getKey(), new IOTypeState(Constants.RULENOTOK.getValue())));                
            Connection.merge(em,commonDatapointValidationResult);
            return commonDatapointValidationResult;
        }
        return null;
    }

    private int persistIntoValidationsDashboard(ConnectionManager cm, IO io) {
        int result = -1;
        JPA<Object[]> jpa = new JPA<>(cm, Object[].class);
 
        try {
            result = jpa.executeFileQuery("InsertIntoValidationsDashboard.sql",
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
                    "format",Constants.ISOBASEFORMAT8601SQLite
                );
        } catch (Exception e) {
            LOG.error("Ocorreu um erro ao realizar ao inserir os dados no Dashboard de Validacoes: " + e.getMessage());
            result = -1;
        }
        
        return result;
    }

}
