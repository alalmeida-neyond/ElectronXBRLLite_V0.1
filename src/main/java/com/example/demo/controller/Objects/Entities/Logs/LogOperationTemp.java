package com.example.demo.controller.Objects.Entities.Logs;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = "LOG_OPERATIONSTEMP", schema = "DPM_OD")
public class LogOperationTemp implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_OPERATIONLOGS_SEQ")
    @SequenceGenerator(name = "LOG_OPERATIONLOGS_SEQ", sequenceName = "DPM_OD.LOG_OPERATIONLOGS_SEQ", allocationSize = 1)
    @Column(name = "LOGID")
    @NotNull
    private Integer logID;
    
    @Column(name = "OPERATIONVID")
    private Integer operationvid;
    
    @Column(name = "OPERATIONNODEID")
    private Integer operationnodeid;
    
    @Column(name = "RESULT")
    @Lob
    private String result;
    
    @Column(name = "MARGINS")
    private String margins;
    
    @Column(name = "KEY")
    private String key;
    
    @Column(name = "LOGTYPE")
    private String logType;
    
    @Column(name = "TIMESTAMP")
    private Timestamp timestamp;

    public LogOperationTemp() {
    }

    public LogOperationTemp(String result) {
        this.result = result;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }
    
    public LogOperationTemp(String result, Integer ioid) {
        this.result = result;
        this.operationvid = ioid;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }
    
    public LogOperationTemp(Integer operationvid, Integer operationnodeid, String result, String margins, String key, String type, Timestamp timestamp) {
        this.operationvid = operationvid;
        this.operationnodeid = operationnodeid;
        this.result = result;
        this.margins = margins;
        this.key = key;
        this.logType = type;
        this.timestamp = timestamp;
    }

    public Integer getLogID() {
        return logID;
    }

    public void setLogID(Integer logID) {
        this.logID = logID;
    }
    
    public Integer getOperationvid() {
        return operationvid;
    }

    public void setOperationvid(Integer operationvid) {
        this.operationvid = operationvid;
    }

    public Integer getOperationnodeid() {
        return operationnodeid;
    }

    public void setOperationnodeid(Integer operationnodeid) {
        this.operationnodeid = operationnodeid;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMargins() {
        return margins;
    }

    public void setMargins(String margins) {
        this.margins = margins;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }         
}
