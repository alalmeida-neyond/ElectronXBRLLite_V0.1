package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "VARIABLEVERSION")
public class VariableVersion implements Serializable {
    
    @Id
    @NotNull
    @Column(name = "VARIABLEVID")
    private int variableVID;
    
    @JoinColumn(referencedColumnName = "VARIABLEID", name = "VARIABLEID", nullable = false)
    @ManyToOne
    private Variable variable;
    
    @JoinColumn(referencedColumnName = "PROPERTYID", name = "PROPERTYID", nullable = false)
    @ManyToOne
    private Property property;
    
    @JoinColumn(referencedColumnName = "SUBCATEGORYVID", name = "SUBCATEGORYVID")
    @ManyToOne
    private SubCategoryVersion subCategoryVersion;
    
    @JoinColumn(referencedColumnName = "CONTEXTID", name = "CONTEXTID")
    @ManyToOne
    private Context context;
    
    @JoinColumn(referencedColumnName = "KEYID", name = "KEYID")
    @ManyToOne
    private CompoundKey key;
    
    @Column(name = "ISMULTIVALUED", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isMultiValued;
    
    @Column(name = "CODE")
    @Size(max = 20)
    private String code;
    
    @Column(name = "NAME")
    @Size(max = 50)
    private String name;
    
    @JoinColumn(referencedColumnName = "RELEASEID", name = "STARTRELEASEID", nullable = false, insertable = false, updatable = false)
    @ManyToOne
    private Release startRelease;
    
    @JoinColumn(referencedColumnName = "RELEASEID", name = "ENDRELEASEID")
    @ManyToOne
    private Release endRelease;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getVariableVID() {
        return variableVID;
    }

    public void setVariableVID(int variableVID) {
        this.variableVID = variableVID;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public SubCategoryVersion getSubCategoryVersion() {
        return subCategoryVersion;
    }

    public void setSubCategoryVersion(SubCategoryVersion subCategoryVersion) {
        this.subCategoryVersion = subCategoryVersion;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public CompoundKey getKey() {
        return key;
    }

    public void setKey(CompoundKey key) {
        this.key = key;
    }

    public boolean isIsMultiValued() {
        return isMultiValued;
    }

    public void setIsMultiValued(boolean isMultiValued) {
        this.isMultiValued = isMultiValued;
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

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
    
}
