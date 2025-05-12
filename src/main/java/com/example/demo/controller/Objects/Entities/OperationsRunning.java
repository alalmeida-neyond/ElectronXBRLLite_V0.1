package com.example.demo.controller.Objects.Entities;

import com.example.demo.controller.Objects.ConfEntities;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "operationrunning")
public class OperationsRunning {

    @Id
    @NotNull    
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "OPERATIONRUNNING_SEQ", sequenceName = "OPERATIONRUNNING_SEQ", allocationSize = 1)
    @Column(name = "OPERATIONRUNNINGID")
    private int operationRunningId;
    
    @Column(name = "USERID")
    private String userId;

    @Column(name = "OPERATION")
    private String operation;

    @Column(name = "THREADNAME", unique = true)
    private String threadName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulovid", referencedColumnName = "MODULEVID", nullable = false)
    private ModuleVersion moduleVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENTITY", referencedColumnName = "ENTITYID", nullable = false)
    private ConfEntities entity;

    @Column(name = "DOMAIN")
    private String domain;

    @Column(name = "REFERENCEDATE")
    private String refDate;

    public OperationsRunning() {
    }

    public OperationsRunning(String userId, String operation, String threadName, ModuleVersion moduleVersion, ConfEntities entity, String domain, String refDate) {
        this.userId = userId;
        this.operation = operation;
        this.threadName = threadName;
        this.moduleVersion = moduleVersion;
        this.entity = entity;
        this.domain = domain;
        this.refDate = refDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public ConfEntities getEntity() {
        return entity;
    }

    public void setEntity(ConfEntities entity) {
        this.entity = entity;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRefDate() {
        return refDate;
    }

    public void setRefDate(String refDate) {
        this.refDate = refDate;
    }

}
