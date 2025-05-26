package com.example.demo.DTOs;

public class OutValidationsDashboardDTO {

    private String refDate;
    private String module;
    private String domain;
    private String entity;
    private Integer mandatoryReportsImported;
    private Integer nOk;
    private Integer nDNRR;
    private Integer nError;
    private Integer nWarning;
    private Integer nProcessNotOk;
    private Integer nTotal;
    private Integer nExpectedToRun;
    private String timestamp;
    private Integer allValidated;

    public OutValidationsDashboardDTO(String refDate, String module, String domain, String entity, Integer mandatoryReportsImported, Integer nOk, Integer nDNRR, Integer nError, Integer nWarning, Integer nProcessNotOk, Integer nTotal, Integer nExpectedToRun, String timestamp, Integer allValidated) {
        this.refDate = refDate;
        this.module = module;
        this.domain = domain;
        this.entity = entity;
        this.mandatoryReportsImported = mandatoryReportsImported;
        this.nOk = nOk;
        this.nDNRR = nDNRR;
        this.nError = nError;
        this.nWarning = nWarning;
        this.nProcessNotOk = nProcessNotOk;
        this.nTotal = nTotal;
        this.nExpectedToRun = nExpectedToRun;
        this.timestamp = timestamp;
        this.allValidated = allValidated;
    }
    
    public String getRefDate() {
        return refDate;
    }

    public void setRefDate(String refDate) {
        this.refDate = refDate;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Integer getMandatoryReportsImported() {
        return mandatoryReportsImported;
    }

    public void setMandatoryReportsImported(Integer mandatoryReportsImported) {
        this.mandatoryReportsImported = mandatoryReportsImported;
    }

    public Integer getNOk() {
        return nOk;
    }

    public void setNOk(Integer nOk) {
        this.nOk = nOk;
    }

    public Integer getNDNRR() {
        return nDNRR;
    }

    public void setNDNRR(Integer nDNRR) {
        this.nDNRR = nDNRR;
    }

    public Integer getNError() {
        return nError;
    }

    public void setNError(Integer nError) {
        this.nError = nError;
    }

    public Integer getNWarning() {
        return nWarning;
    }

    public void setNWarning(Integer nWarning) {
        this.nWarning = nWarning;
    }

    public Integer getNProcessNotOk() {
        return nProcessNotOk;
    }

    public void setNProcessNotOk(Integer nProcessNotOk) {
        this.nProcessNotOk = nProcessNotOk;
    }

    public Integer getNTotal() {
        return nTotal;
    }

    public void setNTotal(Integer nTotal) {
        this.nTotal = nTotal;
    }

    public Integer getNExpectedToRun() {
        return nExpectedToRun;
    }

    public void setNExpectedToRun(Integer nExpectedToRun) {
        this.nExpectedToRun = nExpectedToRun;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getAllValidated() {
        return allValidated;
    }

    public void setAllValidated(Integer allValidated) {
        this.allValidated = allValidated;
    }

    

    
}
