package com.example.demo.Data.Access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.Data.*;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Entities.Conf.*;
import com.example.demo.controller.Objects.Entities.DPMOrigin.DataType;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.Entities.DPMOrigin.Operator;
import com.example.demo.controller.Objects.Entities.DPMOrigin.OperatorArgument;
import com.example.demo.controller.Objects.Entities.DPMOrigin.TableVersionDPM;
import com.example.demo.controller.Objects.IO.IOState;
import com.example.demo.controller.Objects.Logs.LogValidationProcess;

import jakarta.persistence.EntityManager;

import org.jboss.logging.Logger;


public class Info {

    private final Logger LOG = Logger.getLogger(Info.class.getName());

    private final static Info INSTANCE = new Info();
    private ConnectionManager connectionManager;

    @SuppressWarnings("rawtypes")
    Map<String, List> refData = new HashMap<>();

    private Map<Integer, Operator> operatorById = new HashMap<Integer, Operator>();
    private Map<Integer, OperatorArgument> operatorArgumentById = new HashMap<Integer, OperatorArgument>();
    private Map<Integer, List<TableVersionDPM>> tablesByModule = new HashMap<>();
    private Map<Integer, DataType> dataTypeById = new HashMap<>();
    private Map<Integer, IOState> ioStateByID = new HashMap<>();
    private Map<Integer, ConfAction> confActionByID = new HashMap<>();
    private Map<Integer,Set<Integer>> altGenerationMaps = new HashMap<Integer, Set<Integer>>();

    //private List<LogOperationTemp> operationsLogs = new ArrayList<>();
    private List<LogValidationProcess> validationsLogs = new ArrayList<>();

    private Info() {
    }

    public static Info getInstance() {
        return INSTANCE;
    }

    public void loadRefData(Boolean isReload) {
        if(this.refData.isEmpty() || isReload){
            this.connectionManager = new ConnectionManager(Connection.getEm());

            setModuleVersionAll(connectionManager.em);
            setDomainsAll(connectionManager.em);
            setEntitiesAll(connectionManager.em);
            setAppConfigsAll(connectionManager.em);
            setOperatorAll(connectionManager.em);
            setOperatorArgumentAll(connectionManager.em);
            setDataTypeAll(connectionManager.em);
            setIOStateAll(connectionManager.em);
            setConfActionAll(connectionManager.em);
            setTablesByModule(connectionManager.em);
            setConfImportRulesAll(connectionManager.em);
            setVersionsAll(connectionManager.em);
            setPeriocity(connectionManager.em);
            setAltGenerationModules(connectionManager.em);
            this.connectionManager.close();
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T> List<T> refDataGet(String type) {
        return Info.getInstance().getRefData().get(type.toLowerCase());
    }

    public void setModuleVersionAll(EntityManager em) {
        try {
            this.refData.put(Constants.ModuleVersionAll.toLowerCase(), em.createNamedQuery("ModuleVersion.findAll", ModuleVersion.class).getResultList());
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos ModuleVersion:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setIOStateAll(EntityManager em) {
        try {
            List<IOState> auxList =em.createNamedQuery("IOState.findAll", IOState.class).getResultList();
            this.refData.put(Constants.IOStateAll.toLowerCase(), auxList);
            ioStateByID = auxList.stream().collect(Collectors.toMap(IOState::getIoStateId, ioState -> ioState));
            
        } catch (Exception e) {
             LOG.error("Erro na obtencao dos objetos IOState:" + e.getMessage());
        }
    }

    public void setConfActionAll(EntityManager em) {
        try {
            List<ConfAction> auxList =em.createNamedQuery("ConfAction.findAll", ConfAction.class).getResultList();
            this.refData.put(Constants.ConfActionAll.toLowerCase(), auxList);
            confActionByID = auxList.stream().collect(Collectors.toMap(ConfAction::getActionId, confAction -> confAction));
            
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos ConfAction:" + e.getMessage());
        }
    }

    public void setOperatorAll(EntityManager em) {
        try {
            this.refData.put(Constants.OperatorAll.toLowerCase(), em.createNamedQuery("Operator.findAll", Operator.class).getResultList());

            List<Operator> operators = new ArrayList<>(refDataGet(Constants.OperatorAll.toLowerCase()));
            operatorById = operators.stream().collect(Collectors.toMap(Operator::getOperatorID, operator -> operator));
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos Operator:" + e.getMessage());
        }

    }

    public void setOperatorArgumentAll(EntityManager em) {
        try {
            this.refData.put(Constants.OperatorArgumentAll.toLowerCase(), em.createNamedQuery("OperatorArgument.findAll", OperatorArgument.class).getResultList());

            List<OperatorArgument> operatorArguments = new ArrayList<>(refDataGet(Constants.OperatorArgumentAll.toLowerCase()));
            operatorArgumentById = operatorArguments.stream().collect(Collectors.toMap(OperatorArgument::getArgumentID, operatorArgument -> operatorArgument));
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos OperatorArgument:" + e.getMessage());
        }
    }

    public void setAppConfigsAll(EntityManager em) {
        try {
            List<ConfAppConfigs> auxList = em.createNamedQuery("ConfAppConfigs.findAll", ConfAppConfigs.class).getResultList();
            this.refData.put(Constants.AppConfigsAll.toLowerCase(), auxList);
            
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos ConfAppConfig:" + e.getMessage());
        }
    }

    public void setAltGenerationModules(EntityManager em){
        try {
            List<Object[]> auxList = em.createNativeQuery("Select generationtype, ModuleVId from DPM_OD.CONF_ALTGENERATION").getResultList();
            /*altGenerationMaps = auxList.stream().collect(Collectors.groupingBy(
                                                    obj -> ((BigDecimal)obj[0]).intValue(),
                                                    Collectors.mapping(obj -> ((BigDecimal)obj[1]).intValue(), Collectors.toSet())
                                                ));*/
            altGenerationMaps = auxList.stream().collect(Collectors.groupingBy(
                                                    obj -> ((Number)obj[0]).intValue(),
                                                    Collectors.mapping(obj -> ((Number)obj[1]).intValue(), Collectors.toSet())
                                                ));
            
            if(altGenerationMaps == null || altGenerationMaps.isEmpty()){
                LOG.error("Erro ao definir os Modulos da Geracao Alternativa");
            }
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos Geracao alternativa:" + e.getMessage());
        }
    }

    public void setDataTypeAll(EntityManager em) {
        try {
            this.refData.put(Constants.DATATYPEALL.toLowerCase(), em.createNamedQuery("DataType.findAll", DataType.class).getResultList());
            
            List<DataType> dataTypes = new ArrayList<>(refDataGet(Constants.DATATYPEALL.toLowerCase()));
            dataTypeById = dataTypes.stream().collect(Collectors.toMap(DataType::getDataTypeId, dataType -> dataType));
            
            if(dataTypeById == null || dataTypeById.isEmpty()){
                LOG.error("Erro na obtencao dos objetos DataType.");
            }
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos DataType:" + e.getMessage());
        }
    }

    public void setDomainsAll(EntityManager em) {

        List<String> domainsList = new ArrayList<>();
        try {
            domainsList.add(Constants.Consolidado);
            domainsList.add(Constants.Individual);
            this.refData.put(Constants.DomainsAll.toLowerCase(), domainsList);
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos Domain:" + e.getMessage());
        }
    }

    public void setEntitiesAll(EntityManager em) {
        List<ConfEntities> list = em.createNamedQuery("ConfEntities.findAll", ConfEntities.class).getResultList();
        try {
            this.refData.put(Constants.EntitiesAll.toLowerCase(), em.createNamedQuery("ConfEntities.findAll", ConfEntities.class).getResultList());
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos ConfEntities:" + e.getMessage());
        }
    }
    
    public void setVersionsAll(EntityManager em) {
        try {
            this.refData.put(Constants.VersionsAll.toLowerCase(), em.createNativeQuery("SELECT DISTINCT e.versionNumber " +
                                                                                            "FROM ModuleVersion e " +
                                                                                            "ORDER BY " +
                                                                                            "CAST(SUBSTR(e.versionNumber, 1, INSTR(e.versionNumber, '.') - 1) AS INTEGER) DESC, " +
                                                                                            "CAST(SUBSTR( " +
                                                                                            "e.versionNumber, " +
                                                                                            "INSTR(e.versionNumber, '.') + 1, " +
                                                                                            "INSTR(SUBSTR(e.versionNumber, INSTR(e.versionNumber, '.') + 1), '.') - 1 " +
                                                                                            ") AS INTEGER) DESC, " +
                                                                                            "CAST(SUBSTR( " +
                                                                                            "e.versionNumber, " +
                                                                                            "LENGTH(SUBSTR(e.versionNumber, 1, INSTR(e.versionNumber, '.') + INSTR(SUBSTR(e.versionNumber, INSTR(e.versionNumber, '.') + 1), '.') )) + 1 " +
                                                                                            ") AS INTEGER) DESC").getResultList());
        } catch (Exception e) {
            LOG.error("Erro na obtencao da lista de versões:" + e.getMessage());
        }
    }
    
    public void setConfImportRulesAll(EntityManager em) {
        try {
            this.refData.put(Constants.ConfImportRulesAll.toLowerCase(), em.createNamedQuery("ConfImportRules.findAll", ConfImportRules.class).getResultList());
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos ConfEntities:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setTablesByModule(EntityManager em) {
        JPA<Object[]> jpa = new JPA<Object[]>(em, Object[].class);
        List<Object[]> results = new ArrayList<>();
        String query = Utils.getResource("SQL_Queries/TablesByModule.sql");
        try {
            results = jpa.getNativeResultListWithMapping(query, "TablesByModuleMapping");
            for (Object[] result : results) {
                TableVersionDPM table = (TableVersionDPM) result[0];
                Integer moduleVId = Integer.valueOf(result[1].toString());

                if (!tablesByModule.containsKey(moduleVId)) {
                    tablesByModule.put(moduleVId, new ArrayList<>());
                }

                tablesByModule.get(moduleVId).add(table);
            }

        } catch (Exception e) {
            LOG.error("Erro na obtencao dos mapas por módulo:" + e.getMessage());
        }

    }
    
     private void setPeriocity(EntityManager em) {
        try {
            this.refData.put(Constants.PERIODICITYAll.toLowerCase(), em.createNamedQuery("ConfPeriodicity.findAll", ConfPeriodicity.class).getResultList());
        } catch (Exception e) {
            LOG.error("Erro na obtencao dos objetos CONF_PERIODICITY:" + e.getMessage());
        }
     }

    public Map<String, List> getRefData() {
        return refData;
    }

    public Map<Integer, Operator> getOperatorById() {
        return operatorById;
    }

    public Map<Integer, OperatorArgument> getOperatorArgumentById() {
        return operatorArgumentById;
    }


    public String getConfigValueByKey(String key) {
        List<ConfAppConfigs> configsList = refData.get(Constants.AppConfigsAll.toLowerCase());
        Map<String, ConfAppConfigs> appConfigs = configsList.stream().collect(Collectors.toMap(ConfAppConfigs::getKey, appConfig -> appConfig));
        return appConfigs.get(key).getValue();
    }

    public Boolean checkIfUsesAltGeneration(Integer moduleVID, Integer generationToCheck){
        return this.altGenerationMaps.get(generationToCheck).contains(moduleVID);
    }
    public IOState getIOStateByID(Integer ioStateID) {
        return this.ioStateByID.get(ioStateID);
    }
    
    public ConfAction getConfActionByID(Integer confActionID) {
        return this.confActionByID.get(confActionID);
    }
    
    public DataType getDataTypeByID(Integer dataTypeID) {
        return this.dataTypeById.get(dataTypeID);
    }

    public List<TableVersionDPM> getTableVersionByModule(int moduleVId) {
        return tablesByModule.get(moduleVId);
    }

    public List<LogValidationProcess> getValidationsLogs() {
        return validationsLogs;
    }

    public void setValidationsLogs(List<LogValidationProcess> validationsLogs) {
        this.validationsLogs = validationsLogs;
    }
}
