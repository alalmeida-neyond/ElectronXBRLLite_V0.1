/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.DTOs;


public class ImportedFilesResumeDTO {
    private String module;
    private String entity;
    private String domain;
    private String referenceDate;
    private String tablecode;
    private String desagCode;
    private Integer isMandatory;
    private Integer isImported;

    public ImportedFilesResumeDTO(String module, String entity, String domain, String referenceDate, String tablecode, String desagCode, Integer isMandatory, Integer isImported) {
        this.module = module;
        this.entity = entity;
        this.domain = domain;
        this.referenceDate = referenceDate;
        this.tablecode = tablecode;
        this.desagCode = desagCode;
        this.isMandatory = isMandatory;
        this.isImported = isImported;
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

    public Integer getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Integer isMandatory) {
        this.isMandatory = isMandatory;
    }

    public Integer getIsImported() {
        return isImported;
    }

    public void setIsImported(Integer isImported) {
        this.isImported = isImported;
    }

    
}
