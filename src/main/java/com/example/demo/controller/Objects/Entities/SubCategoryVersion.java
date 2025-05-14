package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "SUBCATEGORYVERSION")
public class SubCategoryVersion implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "SUBCATEGORYVID")
    private int subCategoryVID;
    
    @JoinColumn(referencedColumnName = "SUBCATEGORYID", name = "SUBCATEGORYID", nullable = false)
    @ManyToOne
    private SubCategory subCategory;
    
    @JoinColumn(referencedColumnName = "RELEASEID", name = "STARTRELEASEID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Release startRelease;
    
    @JoinColumn(referencedColumnName = "RELEASEID", name = "ENDRELEASEID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Release endRelease;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getSubCategoryVID() {
        return subCategoryVID;
    }

    public void setSubCategoryVID(int subCategoryVID) {
        this.subCategoryVID = subCategoryVID;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
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
