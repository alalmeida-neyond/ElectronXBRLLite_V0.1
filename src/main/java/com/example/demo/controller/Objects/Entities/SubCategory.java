package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "SUBCATEGORY")
public class SubCategory implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "SUBCATEGORYID")
    private int subCategoryID;
    
    @JoinColumn(referencedColumnName = "CATEGORYID", name = "CATEGORYID", nullable = false)
    @ManyToOne
    private Category category;
    
    @Column(name = "CODE")
    @NotNull
    @Size(max = 20)
    private String code;
    
    @Column(name = "NAME")
    //@Lob
    private String name;
    
    @Column(name = "DESCRIPTION")
    //@Lob
    private String description;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)", nullable = false)
    //@OneToOne(fetch = FetchType.LAZY)
    @OneToOne
    private Concept concept;

    public int getSubCategoryID() {
        return subCategoryID;
    }

    public void setSubCategoryID(int subCategoryID) {
        this.subCategoryID = subCategoryID;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
}
