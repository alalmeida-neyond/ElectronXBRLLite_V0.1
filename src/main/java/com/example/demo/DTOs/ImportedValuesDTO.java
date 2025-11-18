
package com.example.demo.DTOs;

public class ImportedValuesDTO {
    
    private String module;
    private String table;
    private String referenceDate;
    private String entity;
    private String domain;
    private String linha;
    private String coluna;
    private String desagregationCode;
    private String rowKey;
    private String ruleValue;
    private String userId;

    public ImportedValuesDTO(String module, String table, String referenceDate, String entity, String domain, String linha, String coluna, String desagregationCode, String rowKey, String ruleValue, String userId) {
        this.module = module;
        this.table = table;
        this.referenceDate = referenceDate;
        this.entity = entity;
        this.domain = domain;
        this.linha = linha;
        this.coluna = coluna;
        this.desagregationCode = desagregationCode;
        this.rowKey = rowKey;
        this.ruleValue = ruleValue;
        this.userId = userId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(String referenceDate) {
        this.referenceDate = referenceDate;
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

    public String getLinha() {
        return linha;
    }

    public void setLinha(String linha) {
        this.linha = linha;
    }

    public String getColuna() {
        return coluna;
    }

    public void setColuna(String coluna) {
        this.coluna = coluna;
    }

    public String getDesagregationCode() {
        return desagregationCode;
    }

    public void setDesagregationCode(String desagregationCode) {
        this.desagregationCode = desagregationCode;
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    
    

}
