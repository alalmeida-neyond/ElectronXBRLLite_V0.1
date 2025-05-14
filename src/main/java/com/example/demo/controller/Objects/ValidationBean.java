package com.example.demo.controller.Objects;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.example.demo.controller.Objects.DAL.*;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.controller.Objects.Import.*;

import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;

import jakarta.faces.view.ViewScoped;
/**
 *
 * @author tbatista
 */
@Named(value = "importFileBean")
@ViewScoped
public class ValidationBean extends DefaultBean {
    
    private List<InImportedTablesTemp> importedTables;
    private List<Integer> selectedMapsToValidate;
    private List<IO> validateIOs;
    private List<IO> validationIOs;
    
    public ValidationBean(){
    }

    @PostConstruct
    public void init() {
        getValidationIOs();
    }
    
    public void getValidationIOs() {
        LocalDate referenceDate = null;
        if (getYear() != null && getMonth() != null) {
            referenceDate = getDateAtLastDay(getMonth(),getYear());
        } else {
            referenceDate = null;
        }
        String domain = getDomain()!= null ? getDomain().length() > Constants.DOMAINLENGTH ? getDomain().substring(0, 3).toUpperCase() : getDomain().toUpperCase() : null;

        //this.lazy = IODAL.getIOsByAction(getModuleVersion() == null ? null : getModuleVersion().getModuleVID(), referenceDate, getEntity() == null ? null : getEntity().getEntityID(), domain, Constants.actionValidation, Constants.VALIDATEID, session.getUser().getUserId(), session.getUser().getProfile().equals(Constants.Admin) ? true : false);
        validationIOs = IODAL.getIOsByAction(getModuleVersion() == null ? null : getModuleVersion().getModuleVID(), referenceDate, getEntity() == null ? null : getEntity().getEntityID(), domain, Constants.actionValidation, Constants.VALIDATEID);
    }
    
    public List<InImportedTablesTemp> getImportedMaps(Integer privilege, boolean withView, boolean triggeredByUser){
        LocalDate referenceDate = null;
        if (getYearExecution()!= null && getMonthExecution()!= null) {
            //CastMonth into number
            referenceDate = getDateAtLastDay(getMonthExecution(), getYearExecution());
        } else {
            return new ArrayList<>();
        }
        
        if(getModuleVersionExecution() != null && getDomainExecution() != null && getEntityExecution() != null)
            importedTables = InImportedTablesDAL.getListOfImportedMapsToValidate(getModuleVersionExecution(), referenceDate, getEntityExecution(), getDomainExecution());
        else
            importedTables = new ArrayList<>();
        
        return importedTables;
    }

    public void startValidation() {
        LocalDate referenceDate = null;
        if (getYearExecution()!= null && getMonthExecution()!= null) {
            //CastMonth into number
            referenceDate = getDateAtLastDay(getMonthExecution(), getYearExecution());
        }
        
        if (getModuleVersionExecution() == null || getEntityExecution() == null || getDomainExecution() == null || getYearExecution() == null || getMonthExecution() == null) {
            throwFacesMessage(FacesMessage.SEVERITY_ERROR, Constants.validationError, Constants.missingFilters);
            return;
        }
        String domain = getDomainExecution()!= null ? getDomainExecution().length() > Constants.DOMAINLENGTH ? getDomainExecution().substring(0, 3).toUpperCase() : getDomainExecution().toUpperCase() : null;
        
        if(selectedMapsToValidate.isEmpty()){
            throwFacesMessage(FacesMessage.SEVERITY_ERROR, Constants.validationError, Constants.NOMAPSSELECTED);
            return;
        }
        
        List<IO> operationsRunningFromIO = IODAL.getOperationRunningFromIO(getModuleVersionExecution(), domain, getEntityExecution(), referenceDate.format(Constants.DATEFORMATISO8601));
        if (!operationsRunningFromIO.isEmpty()) {
            throwFacesMessage(FacesMessage.SEVERITY_INFO, Constants.concurrentOperations, Constants.concurrentOperationsDesc);
            return;
        }
        
        List<InImportedTablesTemp> tablesToValidate = new ArrayList<>();
        for (InImportedTablesTemp importedTable : importedTables) {
            if(selectedMapsToValidate.contains(importedTable.getImportedTableId()))
                tablesToValidate.add(importedTable);
        }
        
        Set<TableVersionDPM> sortedTables = tablesToValidate.stream()
                                                .map(InImportedTablesTemp::getTableVersion)
                                                .collect(Collectors.toCollection(() -> 
                                                    new TreeSet<>(Comparator.comparing(TableVersionDPM::getCode))
                                                ));

        throwFacesMessage(FacesMessage.SEVERITY_INFO, "Validação Iniciada", "Processo de validação iniciada.");
        
        try {
            Validator_2_0 validator = new Validator_2_0(getModuleVersionExecution(), referenceDate, getEntityExecution(), domain, session.getUser().getUserId().toUpperCase(), sortedTables);
            Thread validationThread = new Thread(validator);
            validationThread.start();
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

}
