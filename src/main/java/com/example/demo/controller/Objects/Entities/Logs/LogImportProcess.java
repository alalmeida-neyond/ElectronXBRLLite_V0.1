
package com.example.demo.controller.Objects.Entities.Logs;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.demo.Converter.LocalDateTimePersistenceConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "LOG_IMPORTPROCESS", schema = "DPM_OD")
public class LogImportProcess implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_IMPORTPROCESS_SEQ")
    @SequenceGenerator(name = "LOG_IMPORTPROCESS_SEQ", sequenceName = "DPM_OD.LOG_IMPORTPROCESS_SEQ", allocationSize = 1)
    @Column(name = "LOGIMPORTPROCESSID")
    @NotNull
    private Integer logImportProcessId;
    
    @Column(name = "IOID")
    @NotNull
    private Integer ioID;
    
    @Column(name = "IMPORTEDTABLEID")
    private Integer importedTableId;
    
    @Column(name = "DESCRIPTION")
    @Size(max = 255)
    @NotNull
    private String description;
    
    @Column(name = "TIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime timeStampCreated;

    public LogImportProcess(){}

    public LogImportProcess(Integer ioID, Integer importedTableId, String description, LocalDateTime timeStampCreated) {
        this.ioID = ioID;
        this.importedTableId = importedTableId;
        this.description = description;
        this.timeStampCreated = timeStampCreated;
    }
    
    public LogImportProcess(Integer ioID,Integer importedTableId, String description) {
        this.ioID = ioID;
        this.importedTableId = importedTableId;
        this.description = description;
        this.timeStampCreated = LocalDateTime.now();
    }
    
    public LogImportProcess(Integer ioID, String description) {
        this.ioID = ioID;
        this.description = description;
        this.timeStampCreated = LocalDateTime.now();
    }

    public Integer getIoID() {
        return ioID;
    }

    public void setIoID(Integer ioID) {
        this.ioID = ioID;
    }

    public Integer getImportedTableId() {
        return importedTableId;
    }

    public void setImportedTableId(Integer importedTableId) {
        this.importedTableId = importedTableId;
    }    

    public Integer getLogImportProcessId() {
        return logImportProcessId;
    }

    public void setLogImportProcessId(Integer logImportProcessId) {
        this.logImportProcessId = logImportProcessId;
    }

    public Integer getImportedTable() {
        return importedTableId;
    }

    public void setImportedTable(Integer importedTable) {
        this.importedTableId = importedTable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimeStampCreated() {
        return timeStampCreated;
    }

    public void setTimeStampCreated(LocalDateTime timeStampCreated) {
        this.timeStampCreated = timeStampCreated;
    }
    
    
    
}
