/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Logs;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.demo.Converter.LocalDateTimePersistenceConverter;
import com.example.demo.controller.Objects.Entities.*;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "LOG_VALIDATIONPROCESS")
public class LogValidationProcess implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "LOG_VALIDATIONPROCESS_SEQ", sequenceName = "DPM_ED.LOG_VALIDATIONPROCESS_SEQ", allocationSize = 1)
    @Column(name = "LOGVALIDATIONPROCESSID")
    @NotNull
    private Integer logValidationProcessId;
    
    @JoinColumn(referencedColumnName = "IOID", name = "IOID")
    @ManyToOne
    private IO io;
    
    @Column(name = "DESCRIPTION")
    @Size(max = 255)
    @NotNull
    private String description;
    
    @Column(name = "TIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime timeStampCreated;

    public LogValidationProcess(){}
    
    public LogValidationProcess(String description, LocalDateTime timestamp){
        this.description = description;
        this.timeStampCreated = timestamp;
    }
    
    public LogValidationProcess(IO io, String description, LocalDateTime timestamp){
        this.io = io;
        this.description = description;
        this.timeStampCreated = timestamp;
    }
    
    public Integer getLogValidationProcessId() {
        return logValidationProcessId;
    }

    public void setLogValidationProcessId(Integer logValidationProcessId) {
        this.logValidationProcessId = logValidationProcessId;
    }

    public IO getIo() {
        return io;
    }

    public void setIo(IO io) {
        this.io = io;
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
