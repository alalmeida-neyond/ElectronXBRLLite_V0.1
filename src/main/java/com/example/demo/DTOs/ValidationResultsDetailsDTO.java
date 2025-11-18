
package com.example.demo.DTOs;

import jakarta.persistence.Lob;

public class ValidationResultsDetailsDTO {

    
    private String module;
    private String entity;
    private String domain;
    private String referenceDate;
    private String regraCode;
    private String severity;
    
    @Lob
    private String regraDomain;
    
    @Lob
    private String regra;
    
    @Lob
    private String regraExecutada;
    private String origem;
    private String resultado;
    private String dataProcessamento;
    private String difference;
    private String usedMargin;

    public ValidationResultsDetailsDTO() {
    }

    public ValidationResultsDetailsDTO(String module, String entity, String domain, String referenceDate, String regraCode, String severity, String regraDomain, String regra, String regraExecutada, String origem, String resultado, String dataProcessamento, String difference, String usedMargin) {
        this.module = module;
        this.entity = entity;
        this.domain = domain;
        this.referenceDate = referenceDate;
        this.regraCode = regraCode;
        this.severity = severity;
        this.regraDomain = regraDomain;
        this.regra = regra;
        this.regraExecutada = regraExecutada;
        this.origem = origem;
        this.resultado = resultado;
        this.dataProcessamento = dataProcessamento;
        this.difference = difference;
        this.usedMargin = usedMargin;
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

    public String getRegraCode() {
        return regraCode;
    }

    public void setRegraCode(String regraCode) {
        this.regraCode = regraCode;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getRegraDomain() {
        return regraDomain;
    }

    public void setRegraDomain(String regraDomain) {
        this.regraDomain = regraDomain;
    }

    public String getRegra() {
        return regra;
    }

    public void setRegra(String regra) {
        this.regra = regra;
    }

    public String getRegraExecutada() {
        return regraExecutada;
    }

    public void setRegraExecutada(String regraExecutada) {
        this.regraExecutada = regraExecutada;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getDataProcessamento() {
        return dataProcessamento;
    }

    public void setDataProcessamento(String dataProcessamento) {
        this.dataProcessamento = dataProcessamento;
    }

    public String getDifference() {
        return difference;
    }

    public void setDifference(String difference) {
        this.difference = difference;
    }

    public String getUsedMargin() {
        return usedMargin;
    }

    public void setUsedMargin(String usedMargin) {
        this.usedMargin = usedMargin;
    }
}
