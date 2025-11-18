
package com.example.demo.controller.Objects.Generation;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.Converter.*;
import com.example.demo.controller.Objects.Entities.Conf.ConfEntities;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.IO.IO;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityResult;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "OUT_XBRLGENERATED", schema = "DPM_OD")
@SqlResultSetMapping(
        name = "OutXBRLGeneratedMapping",
        entities = {
            @EntityResult(entityClass = OutXBRLGenerated.class),},
        columns = {
            @ColumnResult(name = "LOCKED", type = Integer.class),
            @ColumnResult(name = "LOCKABLE", type = Integer.class),
            @ColumnResult(name = "LOCKEDBY", type = String.class),
            @ColumnResult(name = "LOCKEDAT", type = String.class),
            @ColumnResult(name = "UNLOCKEDBY", type = String.class),
            @ColumnResult(name = "UNLOCKEDAT", type = String.class),
            @ColumnResult(name = "ASDIFFS", type = Integer.class)}
)
public class OutXBRLGenerated implements Serializable {

    public OutXBRLGenerated() {
    }

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genXBRLSequence_gen")
    @SequenceGenerator(name = "genXBRLSequence_gen", sequenceName = "DPM_OD.GENERATEXBLRSEQUENCE", allocationSize = 1)
    @Column(name = "XBRL_ID")
    private int idXBRLGenerate;

    @Column(name = "USERID")
    private String userId;

    @Column(name = "folderName")
    private String folderName;

    @ManyToOne
    @JoinColumn(name = "MODULEVID", referencedColumnName = "MODULEVID", nullable = false)
    private ModuleVersion moduleVersion;

    @Column(name = "generate_TIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime generateTimestamp;

    @ManyToOne
    @JoinColumn(name = "ENTITYID", referencedColumnName = "ENTITYID", nullable = false)
    private ConfEntities entity;

    @Column(name = "DOMAIN")
    private String domain;

    @Column(name = "REF_DATE")
    @Convert(converter = LocalDatePersistenceConverter.class)  
    private LocalDate referenceDate;

    @ManyToOne
    @JoinColumn(name = "IOID", referencedColumnName = "IOID", nullable = false)
    private IO io;

    @Transient
    private Boolean locked;

    @Transient
    private Boolean lockable;

    @Transient
    private String lockedby;

    @Transient
    private String lockedat;

    @Transient
    private String unlockedby;

    @Transient
    private String unlockedat;
    
    @Transient
    private Boolean asDiffs;

    public OutXBRLGenerated(String userId, String folderName, ModuleVersion moduleVersion, LocalDateTime generateTimestamp,
            ConfEntities entity, String domain, LocalDate referenceDate, IO io) {
        this.userId = userId;
        this.folderName = folderName;
        this.moduleVersion = moduleVersion;
        this.generateTimestamp = generateTimestamp;
        this.entity = entity;
        this.domain = domain;
        this.referenceDate = referenceDate;
        this.io = io;
    }

    public OutXBRLGenerated(String userId, String folderName, ModuleVersion moduleVersion, LocalDateTime generateTimestamp,
            ConfEntities entity, String domain, LocalDate referenceDate, IO io, Boolean locked, Boolean lockable) {
        this.userId = userId;
        this.folderName = folderName;
        this.moduleVersion = moduleVersion;
        this.generateTimestamp = generateTimestamp;
        this.entity = entity;
        this.domain = domain;
        this.referenceDate = referenceDate;
        this.io = io;
        this.locked = locked;
        this.lockable = lockable;
    }

    public int getIdXBRLGenerate() {
        return idXBRLGenerate;
    }

    public void setIdXBRLGenerate(int idXBRLGenerate) {
        this.idXBRLGenerate = idXBRLGenerate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public LocalDateTime getGenerateTimestamp() {
        return generateTimestamp;
    }

    public void setGenerateTimestamp(LocalDateTime generateTimestamp) {
        this.generateTimestamp = generateTimestamp;
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

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public IO getIo() {
        return io;
    }

    public void setIo(IO io) {
        this.io = io;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getLockable() {
        return lockable;
    }

    public void setLockable(Boolean lockable) {
        this.lockable = lockable;
    }

    public String getLockedby() {
        return lockedby;
    }

    public void setLockedby(String lockedby) {
        this.lockedby = lockedby;
    }

    public String getLockedat() {
        return lockedat;
    }

    public void setLockedat(String lockedat) {
        this.lockedat = lockedat;
    }

    public String getUnlockedby() {
        return unlockedby;
    }

    public void setUnlockedby(String unlockedby) {
        this.unlockedby = unlockedby;
    }

    public String getUnlockedat() {
        return unlockedat;
    }

    public void setUnlockedat(String unlockedat) {
        this.unlockedat = unlockedat;
    }

    public Boolean getAsDiffs() {
        return asDiffs;
    }

    public void setAsDiffs(Boolean asDiffs) {
        this.asDiffs = asDiffs;
    }
    
}
