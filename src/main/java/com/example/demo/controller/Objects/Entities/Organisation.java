package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "ORGANISATION")
public class Organisation implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "ORGID")
    private int organisationID;
    
    @Column(name = "NAME")
    @NotNull
    @Size(max = 200)
    private String name;
    
    @Column(name = "ACRONYM")
    @Size(max = 20)
    private String acronym;
    
    @Column(name = "IDPREFIX")
    @NotNull
    private int idPrefix;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "CONCEPTGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getOrganisationID() {
        return organisationID;
    }

    public void setOrganisationID(int organisationID) {
        this.organisationID = organisationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public int getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(int idPrefix) {
        this.idPrefix = idPrefix;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
}
