package com.example.demo.controller.Objects.Entities.Logs;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "LOG_OPERATIONSTEMP")
public class LogOperationTemp implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "LOG_OPERATIONLOGS_SEQ", sequenceName = "LOG_OPERATIONLOGS_SEQ", allocationSize = 1)
    @Column(name = "LOGID")
    @NotNull
    private Integer logID;
    
    @Column(name = "IOID")
    @NotNull
    private Integer ioid;

    @Column(name = "DESCRIPTION")
    @NotNull
    //@Lob
    private String result;
    
    @Column(name = "TIMESTAMP")
    @NotNull
    private Timestamp timestamp;

    public LogOperationTemp() {
    }

    public LogOperationTemp(String result) {
        this.result = result;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }
    
    public LogOperationTemp(String result, Integer ioid) {
        this.result = result;
        this.ioid = ioid;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Integer getLogID() {
        return logID;
    }

    public void setLogID(Integer logID) {
        this.logID = logID;
    }

    public Integer getIoid() {
        return ioid;
    }

    public void setIoid(Integer ioid) {
        this.ioid = ioid;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }     
}
