
package com.example.demo.DTOs;

public class ValidationResumesDTO {

    private String module;
    private String entity;
    private String domain;
    private String referenceDate;
    private String tablecode;
    private String desagCode;
    private Integer isValidated;

    public ValidationResumesDTO(){}
    
    public ValidationResumesDTO(String module, String entity, String domain, String referenceDate, String tablecode, String desagCode, Integer isValidated) {
        this.module = module;
        this.entity = entity;
        this.domain = domain;
        this.referenceDate = referenceDate;
        this.tablecode = tablecode;
        this.desagCode = desagCode;
        this.isValidated = isValidated;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(String referenceDate) {
        this.referenceDate = referenceDate;
    }

    public String getTablecode() {
        return tablecode;
    }

    public void setTablecode(String tablecode) {
        this.tablecode = tablecode;
    }

    public String getDesagCode() {
        return desagCode;
    }

    public void setDesagCode(String desagCode) {
        this.desagCode = desagCode;
    }

    public Integer getIsValidated() {
        return isValidated;
    }

    public void setIsValidated(Integer isValidated) {
        this.isValidated = isValidated;
    }
}
