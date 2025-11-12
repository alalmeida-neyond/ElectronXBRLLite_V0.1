package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;


import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = "MODULE", schema = "DPM_MD")
public class ModuleDPM implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "MODULEID")
    private int moduleId;
    
    @JoinColumn(referencedColumnName = "FRAMEWORKID", name = "FRAMEWORKID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Framework framework;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;
    
    @Column(name = "ISDOCUMENTMODULE", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isDocument;

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public Framework getFramework() {
        return framework;
    }

    public void setFramework(Framework framework) {
        this.framework = framework;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public boolean isIsDocument() {
        return isDocument;
    }

    public void setIsDocument(boolean isDocument) {
        this.isDocument = isDocument;
    }    
}
