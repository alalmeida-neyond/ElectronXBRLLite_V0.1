package com.example.demo.controller.Objects.Entities;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.Converter.*;
import com.example.demo.controller.Objects.*;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
    

@Entity
@Table(name = "IO")
@NamedQuery(name="IO.findAll", query="SELECT e FROM IO e")
/*@SqlResultSetMappings({   
    @SqlResultSetMapping(
            name = "DashboardGeralRow",
            classes = {
                @ConstructorResult(
                        targetClass = DashboardGeralDTO.class,
                        columns = {
                            @ColumnResult(name = "referenceDate", type = String.class),
                            @ColumnResult(name = "module", type = String.class),
                            @ColumnResult(name = "entity", type = String.class),
                            @ColumnResult(name = "domain", type = String.class),
                            @ColumnResult(name = "isImported", type = Boolean.class),
                            @ColumnResult(name = "isValidated", type = Boolean.class),
                            @ColumnResult(name = "isGenerated", type = Boolean.class),
                            @ColumnResult(name = "isLocked", type = Boolean.class)
                        }
                )
            })
})*/
public class IO implements Serializable{
    
    @Id
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    //@SequenceGenerator(name="IO_IOID_GENERATOR", sequenceName="IO_SEQ", allocationSize=1, initialValue=1)
    @Column(name = "IOID")
    private int ioId;
    
    @ManyToOne
    @JoinColumn(name = "IO_STATEID", referencedColumnName = "IO_STATEID", nullable = false)
    private IOState ioState;     
    
    @ManyToOne
    @JoinColumn(name = "IO_ASSOCIATED")
    private IO ioAssociated;
    
    @Column(name = "REFERENCEDATE")    
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate referenceDate;
    
    @JoinColumn(referencedColumnName = "MODULEVID", name = "MODULEVID", nullable = false)
    @ManyToOne
    private ModuleVersion module;
    
    @Column(name = "DOMAIN")
    private String domain;
    
    @JoinColumn(referencedColumnName = "ENTITYID", name = "ENTITYID", nullable = false)
    @ManyToOne
    private ConfEntities entity;

    @Column(name = "INITTIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime initTimestamp;
    
    @Column(name = "ENDTIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime endTimestamp;
    
    @JoinColumn(referencedColumnName = "ACTIONID", name = "ACTIONID", nullable = false)
    @ManyToOne
    private ConfAction action;
    
    @Column(name = "USERID")
    private String userId; 
    
    @Column(name = "FILENAME")
    private String filename;
    
    @Column(name = "THREADNAME")
    private String threadFilename;
    
    @Column(name = "FILENAMESERVER")
    private String filenameserver;

    public IO() {
    }    

    public IO(IOState ioState, LocalDate referenceDate, ModuleVersion module, String domain, ConfEntities entity, LocalDateTime initTimestamp, LocalDateTime endTimestamp, ConfAction action, String userId, String filename, String threadFilename, String filenameserver) {
        this.ioState = ioState; 
        this.referenceDate = referenceDate;
        this.module = module;
        this.domain = domain;
        this.entity = entity;
        this.initTimestamp = initTimestamp;
        this.endTimestamp = endTimestamp;
        this.action = action;
        this.userId = userId;
        this.filename = filename;
        this.threadFilename = threadFilename;
        this.filenameserver = filenameserver;
    }

    public IO(IOState ioState, LocalDate referenceDate, ModuleVersion module, String domain, ConfEntities entity, LocalDateTime initTimestamp, ConfAction action, String userId) {
        this.ioState = ioState;
        this.referenceDate = referenceDate;
        this.module = module;
        this.domain = domain;
        this.entity = entity;
        this.initTimestamp = initTimestamp;
        this.action = action;
        this.userId = userId;
    }
    
    public IO(IO ioAssociated, IOState ioState, LocalDate referenceDate, ModuleVersion module, String domain, ConfEntities entity, LocalDateTime initTimestamp, ConfAction action, String userId) {
        this.ioAssociated = ioAssociated;
        this.ioState = ioState;
        this.referenceDate = referenceDate;
        this.module = module;
        this.domain = domain;
        this.entity = entity;
        this.initTimestamp = initTimestamp;
        this.action = action;
        this.userId = userId;
    }
    
    public int getIoId() {
        return ioId;
    }

    public void setIoId(int ioId) {
        this.ioId = ioId;
    }

    public IOState getIoState() {
        return ioState;
    }

    public void setIoState(IOState ioState) {
        this.ioState = ioState;
    }

    public IO getIoAssociated() {
        return ioAssociated;
    }

    public void setIoAssociated(IO ioAssociated) {
        this.ioAssociated = ioAssociated;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
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

    public LocalDateTime getInitTimestamp() {
        return initTimestamp;
    }

    public void setInitTimestamp(LocalDateTime initTimestamp) {
        this.initTimestamp = initTimestamp;
    }

    public LocalDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(LocalDateTime endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public ConfAction getAction() {
        return action;
    }

    public void setAction(ConfAction action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getThreadFilename() {
        return threadFilename;
    }

    public void setThreadFilename(String threadFilename) {
        this.threadFilename = threadFilename;
    }

    public String getFilenameserver() {
        return filenameserver;
    }

    public void setFilenameserver(String filenameserver) {
        this.filenameserver = filenameserver;
    }
    
    
}

