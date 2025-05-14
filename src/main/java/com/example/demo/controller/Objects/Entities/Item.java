package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "ITEM")
public class Item implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "ITEMID")
    private int itemId;
    
    @Column(name = "NAME")
    //@Lob
    @NotNull
    private String name;
    
    @Column(name = "DESCRIPTION")
    //@Lob
    private String description;
    
    //@Column(name = "ISPROPERTY", columnDefinition = "CHAR(1)")
    @Column(name = "ISPROPERTY")
    @NotNull
    private boolean isProperty;
    
    //@Column(name = "ISACTIVE", columnDefinition = "CHAR(1)")
    @Column(name = "ISACTIVE")
    @NotNull
    private boolean isActive;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
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

    public boolean isIsProperty() {
        return isProperty;
    }

    public void setIsProperty(boolean isProperty) {
        this.isProperty = isProperty;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
}
