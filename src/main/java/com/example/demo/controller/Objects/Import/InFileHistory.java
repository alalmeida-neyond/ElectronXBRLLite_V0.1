package com.example.demo.controller.Objects.Import;

import java.time.LocalDateTime;

import com.example.demo.Converter.LocalDateTimePersistenceConverter;
import com.example.demo.controller.Objects.Entities.Conf.ConfEntities;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "IN_FILEHISTORY")
public class InFileHistory {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FILE_ID")
    private Integer id;
    
    @Column(name = "USERID")
    private String userId;

    
    @Column(name = "FILENAME")
    private String filename;
    
    @Column(name = "filenameOnServer")
    private String filenameOnServer;
    
    @ManyToOne
    @JoinColumn(name = "MODULEVID", referencedColumnName = "MODULEVID", nullable = false)
    private ModuleVersion moduleVersion;
    
    @Column(name = "IMPORT_TIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime importTimestamp;
    
    @ManyToOne
    @JoinColumn(name = "ENTITYID", referencedColumnName = "ENTITYID", nullable = false)
    private ConfEntities entity;
    
    @Column(name = "DOMAIN")
    private String domain;
    
    @Column(name = "REF_DATE")
    private String refDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public LocalDateTime getImportTimestamp() {
        return importTimestamp;
    }

    public void setImportTimestamp(LocalDateTime importTimestamp) {
        this.importTimestamp = importTimestamp;
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

    public String getFilenameOnServer() {
        return filenameOnServer;
    }

    public void setFilenameOnServer(String filenameOnServer) {
        this.filenameOnServer = filenameOnServer;
    }

    
    
}