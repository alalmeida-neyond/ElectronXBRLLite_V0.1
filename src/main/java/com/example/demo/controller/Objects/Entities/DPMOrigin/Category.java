package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import org.springframework.data.annotation.Immutable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "CATEGORY", schema = "DPM_MD")
public class Category implements Serializable {
    
    @Id
    @NotNull
    @Column(name = "CATEGORYID")
    private Integer categoryId;
    
    @Column(name = "CODE")
    @NotNull
    @Size(max = 20)
    private String code;
    
    @Column(name = "NAME")
    @NotNull
    @Size(max = 50)
    private String name;
    
    @Column(name = "DESCRIPTION")
    @Lob
    private String description;
    
    @Column(name = "ISENUMERATED", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isEnumerated;
    
    @Column(name = "ISACTIVE", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isActive;
    
    @Column(name = "ISEXTERNALREFDATA", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isExternalRefData;
    
    @Column(name = "REFDATASOURCE")
    @Size(max = 255)
    private String refDataSource;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;
    
    @JoinColumn(referencedColumnName = "RELEASEID", name = "CREATEDRELEASE")
    @ManyToOne(fetch = FetchType.LAZY)
    private Release createdRelease;

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public boolean isIsEnumerated() {
        return isEnumerated;
    }

    public void setIsEnumerated(boolean isEnumerated) {
        this.isEnumerated = isEnumerated;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isIsExternalRefData() {
        return isExternalRefData;
    }

    public void setIsExternalRefData(boolean isExternalRefData) {
        this.isExternalRefData = isExternalRefData;
    }

    public String getRefDataSource() {
        return refDataSource;
    }

    public void setRefDataSource(String refDataSource) {
        this.refDataSource = refDataSource;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }   

    public Release getCreatedRelease() {
        return createdRelease;
    }

    public void setCreatedRelease(Release createdRelease) {
        this.createdRelease = createdRelease;
    }
}
