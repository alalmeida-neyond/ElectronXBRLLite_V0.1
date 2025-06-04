package com.example.demo.controller.Objects.ActionPhases;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import com.example.demo.controller.Objects.*;
import com.example.demo.controller.Objects.Conf.ConfEntities;
import com.example.demo.controller.Objects.DAL.*;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.controller.Objects.Import.*;

public class ValidationAction {
    
    private List<InImportedTablesTemp> importedTables;
    private List<Integer> selectedMapsToValidate;
    private List<IO> validateIOs;
    private List<IO> validationIOs;
    
    private final Logger LOG = Logger.getLogger(ValidationAction.class.getName());

    private RefDataBean refDataBean;

    
    public List<InImportedTablesTemp> getImportedMaps(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename, IO io){
        if(moduleVersion != null && domain != null && entity != null)
            importedTables = InImportedTablesDAL.getListOfImportedMapsToValidate(moduleVersion, referenceDate, entity, domain, io);
        else
            importedTables = new ArrayList<>();
        
        return importedTables;
    }

    public void startValidation(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename, IO io) {
        List<IO> operationsRunningFromIO = IODAL.getOperationRunningFromIO(moduleVersion, domain, entity, referenceDate.toString());
        if (!operationsRunningFromIO.isEmpty()) {
            LOG.info(Constants.concurrentOperations + " | " + Constants.concurrentOperationsDesc);
            return;
        }
        importedTables = getImportedMaps(referenceDate, moduleVersion, domain, entity, filename, io);
        
        List<InImportedTablesTemp> tablesToValidate = new ArrayList<>();
        for (InImportedTablesTemp importedTable : importedTables) {
            tablesToValidate.add(importedTable);
        }
        
        Set<TableVersionDPM> sortedTables = tablesToValidate.stream()
                                                .map(InImportedTablesTemp::getTableVersion)
                                                .collect(Collectors.toCollection(() -> 
                                                    new TreeSet<>(Comparator.comparing(TableVersionDPM::getCode))
                                                ));
        LOG.info("Validacao Iniciada | " + "Processo de validacao iniciada.");
        
        try {
            Validator_2_0 validator = new Validator_2_0(moduleVersion, referenceDate, entity, domain, sortedTables);
            validator.validateOperations(referenceDate, moduleVersion, domain.toUpperCase(), entity, filename, io);
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

    public List<InImportedTablesTemp> getImportedTables() {
        return importedTables;
    }

    public void setImportedTables(List<InImportedTablesTemp> importedTables) {
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

    public RefDataBean getRefDataBean()
    {
        return refDataBean;
    }

    public void setRefData(RefDataBean refDataBean)
    {
        this.refDataBean = refDataBean;
    }

}
