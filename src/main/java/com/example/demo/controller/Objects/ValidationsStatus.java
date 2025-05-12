package com.example.demo.controller.Objects;

public class ValidationsStatus {
    
    private String validatedReports;
    private String totalReportOfTheModule;
    private String lastValidationStatus;    
 

    public String getValidatedReports() { 
        return validatedReports;
    }
    public void setValidatedReports(String validatedReports) {
        this.validatedReports = validatedReports;
    }

    public String getTotalReportOfTheModule() {
        return totalReportOfTheModule;
    }

    public void setTotalReportOfTheModule(String totalReportOfTheModule) {
        this.totalReportOfTheModule = totalReportOfTheModule;
    }

    public String getLastValidationStatus() {
        return lastValidationStatus;
    }

    public void setLastValidationStatus(String lastValidationStatus) {
        this.lastValidationStatus = lastValidationStatus;
    }
}
