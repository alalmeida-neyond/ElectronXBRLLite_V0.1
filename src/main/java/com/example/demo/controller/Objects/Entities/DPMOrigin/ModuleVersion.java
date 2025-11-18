package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import com.example.demo.Converter.*;

@Entity(name = "ModuleVersion")
@Table(name = "MODULEVERSION", schema = "DPM_MD")
@NamedQueries({
    @NamedQuery(name = "ModuleVersion.findAll", query = "SELECT m FROM ModuleVersion m")
})
public class ModuleVersion implements Serializable {

    @Id
    @NotNull
    @Column(name = "MODULEVID")
    private int moduleVID;

    @JoinColumn(referencedColumnName = "MODULEID", name = "MODULEID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ModuleDPM moduleDPM;

    @JoinColumn(referencedColumnName = "KEYID", name = "GLOBALKEYID")
    @ManyToOne(fetch = FetchType.LAZY)
    private CompoundKey globalKey;

    @JoinColumn(referencedColumnName = "RELEASEID", name = "STARTRELEASEID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Release startRelease;

    @JoinColumn(referencedColumnName = "RELEASEID", name = "ENDRELEASEID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Release endRelease;

    @Column(name = "CODE")
    @NotNull
    @Size(max = 40)
    private String code;

    @Column(name = "NAME")
    @NotNull
    @Size(max = 100)
    private String name;

    @Column(name = "DESCRIPTION")
    @Size(max = 255)
    private String description;

    @Column(name = "VERSIONNUMBER")
    @Size(max = 20)
    private String versionNumber;

    @Column(name = "FROMREFERENCEDATE")
    @NotNull
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate fromReferenceDate;

    @Column(name = "TOREFERENCEDATE")
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate toReferenceDate;

    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;
    
    @Column(name = "ISREPORTED", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isReported;
    
    @Column(name = "ISCALCULATED", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isCalculated;

    public ModuleVersion() {
    }

    public int getModuleVID() {
        return moduleVID;
    }

    public void setModuleVID(int moduleVID) {
        this.moduleVID = moduleVID;
    }

    public ModuleDPM getModuleDPM() {
        return moduleDPM;
    }

    public void setModuleDPM(ModuleDPM moduleDPM) {
        this.moduleDPM = moduleDPM;
    }

    public CompoundKey getGlobalKey() {
        return globalKey;
    }

    public void setGlobalKey(CompoundKey globalKey) {
        this.globalKey = globalKey;
    }

    public Release getStartRelease() {
        return startRelease;
    }

    public void setStartRelease(Release startRelease) {
        this.startRelease = startRelease;
    }

    public Release getEndRelease() {
        return endRelease;
    }

    public void setEndRelease(Release endRelease) {
        this.endRelease = endRelease;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public LocalDate getFromReferenceDate() {
        return this.fromReferenceDate == null ? LocalDate.MAX : this.fromReferenceDate;
    }

    public void setFromReferenceDate(LocalDate fromReferenceDate) {
        this.fromReferenceDate = fromReferenceDate;
    }

    public LocalDate getToReferenceDate() {
        return this.toReferenceDate == null ? LocalDate.MAX : this.toReferenceDate;
    }

    public void setToReferenceDate(LocalDate toReferenceDate) {
        this.toReferenceDate = toReferenceDate;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public boolean isIsReported() {
        return isReported;
    }

    public void setIsReported(boolean isReported) {
        this.isReported = isReported;
    }

    public boolean isIsCalculated() {
        return isCalculated;
    }

    public void setIsCalculated(boolean isCalculated) {
        this.isCalculated = isCalculated;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(moduleVID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModuleVersion other = (ModuleVersion) obj;
        return Objects.equals(this.getModuleVID(), other.getModuleVID());
    }

    
}