package com.example.demo.controller.Objects;

public class ValidationResultsStatus {
    
    
    private String reportCode;
    private String reportDate;
    private String numValidationErrors;  
    private String userid;
    private String tipologiaRisco;
    private String relatoriosNaoImportados;
    private int entityID;
    private String entityCode;

    public String getRelatoriosNaoImportados() {
        return relatoriosNaoImportados;
    }

    public void setRelatoriosNaoImportados(String relatoriosNaoImportados) {
        this.relatoriosNaoImportados = relatoriosNaoImportados;
    }

    public String getTipologiaRisco() {
        return tipologiaRisco;
    }

    public void setTipologiaRisco(String tipologia) {
        this.tipologiaRisco = tipologia;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLastChange() {
        return lastChange;
    }

    public void setLastChange(String lastChange) {
        this.lastChange = lastChange;
    }
    private String lastChange;
   

    public String getReportCode() { 
        return reportCode;
    }
    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }

    public String getReportDate() {
        return reportDate;
    }
    
    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }
    
    public void setNumValidationErrors(String numValidationErrors) {
        this.numValidationErrors = numValidationErrors;
    }
    
    public String getNumValidationErrors() {
        return numValidationErrors;
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    } 
}
