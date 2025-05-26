/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.Converter.*;
import com.example.demo.DTOs.OutValidationsDashboardDTO;
import com.example.demo.controller.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "OUT_VALIDATIONSDASHBOARD")
@SqlResultSetMapping(
        name = "ValidationsDashboardResults",
        classes = {
            @ConstructorResult(
                targetClass = OutValidationsDashboardDTO.class,
                columns = {
                    @ColumnResult(name = "refDate", type = String.class),
                    @ColumnResult(name = "module", type = String.class),
                    @ColumnResult(name = "domain", type = String.class),
                    @ColumnResult(name = "entity", type = String.class),
                    @ColumnResult(name = "mandatoryReportsImported", type = Integer.class),
                    @ColumnResult(name = "Ok", type = Integer.class),
                    @ColumnResult(name = "dnrr", type = Integer.class),
                    @ColumnResult(name = "error", type = Integer.class),
                    @ColumnResult(name = "warning", type = Integer.class),
                    @ColumnResult(name = "processNotOk", type = Integer.class),
                    @ColumnResult(name = "total", type = Integer.class),
                    @ColumnResult(name = "expectedToRun", type = Integer.class),
                    @ColumnResult(name = "timestamp", type = String.class),
                    @ColumnResult(name = "allValidated", type = Integer.class),
                }
            )
        }
)

public class OutValidationDashboard implements Serializable{
    
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "OUT_VALIDATIONSDASHBOARD_SEQ", sequenceName = "DPM_ED.OUT_VALIDATIONSDASHBOARD_SEQ", allocationSize = 1)
    @Column(name = "VALIDATIONSDASHBOARDID")
    private int validationsDashboardId;
    
    @Column(name = "REFERENCEDATE")    
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate refDate;
    
    @JoinColumn(referencedColumnName = "MODULEVID", name = "MODULEVID", nullable = false)
    @ManyToOne
    private ModuleVersion module;
    
    @Column(name = "DOMAIN")
    @Size(max = 3)
    private String domain;
    
    @JoinColumn(referencedColumnName = "ENTITYID", name = "ENTITYID", nullable = false)
    @ManyToOne
    private ConfEntities entity;
    
    @Column(name = "MANDATORYREPORTSIMPORTED", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean mandatoryReportsImported;
    
    @Column(name = "OK")
    private int nOk;
    
    @Column(name = "DNRR")
    private int nDNRR;
    
    @Column(name = "ERROR")
    private int nError;    
    
    @Column(name = "WARNING")
    private int nWarning;
    
    @Column(name = "PROCESSNOTOK")
    private int nProcessNotOk;
    
    @Column(name = "TOTAL")
    private int nTotal;
    
    @Column(name = "EXPECTEDTORUN")
    private int nExpectedToRun;
    
    @Column(name = "TIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime timeStamp;

    public int getValidationsDashboardId() {
        return validationsDashboardId;
    }

    public void setValidationsDashboardId(int validationsDashboardId) {
        this.validationsDashboardId = validationsDashboardId;
    }

    public LocalDate getReferenceDate() {
        return refDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.refDate = referenceDate;
    }

    public ModuleVersion getModule() {
        return module;
    }

    public void setModule(ModuleVersion module) {
        this.module = module;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public ConfEntities getEntity() {
        return entity;
    }

    public void setEntity(ConfEntities entity) {
        this.entity = entity;
    }

    public boolean isMandatoryReportsImported() {
        return mandatoryReportsImported;
    }

    public void setMandatoryReportsImported(boolean mandatoryReportsImported) {
        this.mandatoryReportsImported = mandatoryReportsImported;
    }

    public int getnOk() {
        return nOk;
    }

    public void setnOk(int nOk) {
        this.nOk = nOk;
    }

    public int getnDNRR() {
        return nDNRR;
    }

    public void setnDNRR(int nDNRR) {
        this.nDNRR = nDNRR;
    }

    public int getnError() {
        return nError;
    }

    public void setnError(int nError) {
        this.nError = nError;
    }

    public int getnWarning() {
        return nWarning;
    }

    public void setnWarning(int nWarning) {
        this.nWarning = nWarning;
    }

    public int getnProcessNotOk() {
        return nProcessNotOk;
    }

    public void setnProcessNotOk(int nProcessNotOk) {
        this.nProcessNotOk = nProcessNotOk;
    }

    public int getnTotal() {
        return nTotal;
    }

    public void setnTotal(int nTotal) {
        this.nTotal = nTotal;
    }

    public int getnExpectedToRun() {
        return nExpectedToRun;
    }

    public void setnExpectedToRun(int nExpectedToRun) {
        this.nExpectedToRun = nExpectedToRun;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    
    
    
}
