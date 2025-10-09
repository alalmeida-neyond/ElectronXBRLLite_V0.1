package com.example.demo.DTOs;


/**
 *
 * @author gjardim
 */
public class GenerateXBRLDetailsDTO {
    
    private String code;
    private String entity;
    private String domain;
    private String referenceDate;
    private String description;
    private String timestamp;
    
    public GenerateXBRLDetailsDTO(String code, String entity, String domain, String referenceDate, String description, String timestamp) {
        this.code = code;
        this.entity = entity;
        this.domain = domain;
        this.referenceDate = referenceDate;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
}
