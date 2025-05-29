package com.example.demo.DTOs;


public class ValidationResultsDTO {

    private String validationResultID;
    private String refDate;
    private String modulo;
    private String entity;
    private String domain;
    private String relatorio;
    private String severity;
    private String regra;
    private String origem;
    private String resultado;
    private String dataProcessamento;
    private String user;
    private Integer ioId;

    public ValidationResultsDTO() {
    }

    public ValidationResultsDTO(String operationVid, String refDate, String modulo, String entity, String domain, String relatorio, String severity, String regra, String origem, String resultado, String dataProcessamento, String user, Integer ioId) {
        this.validationResultID = operationVid;
        this.refDate = refDate;
        this.modulo = modulo;
        this.entity = entity;
        this.domain = domain;
        this.relatorio = relatorio;
        this.severity = severity;
        this.regra = regra;
        this.origem = origem;
        this.resultado = resultado;
        this.dataProcessamento = dataProcessamento;
        this.user = user;
        this.ioId = ioId;
    }

    public String getRefDate() {
        return refDate;
    }

    public void setRefDate(String refDate) {
        this.refDate = refDate;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
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

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getRegra() {
        return regra;
    }

    public void setRegra(String regra) {
        this.regra = regra;
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

    public String getRelatorio() {
        return relatorio;
    }

    public void setRelatorio(String relatorio) {
        this.relatorio = relatorio;
    }

    public String getValidationResultID() {
        return validationResultID;
    }

    public void setValidationResultID(String validationResultID) {
        this.validationResultID = validationResultID;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getIoId() {
        return ioId;
    }

    public void setIoId(Integer ioId) {
        this.ioId = ioId;
    }

}
