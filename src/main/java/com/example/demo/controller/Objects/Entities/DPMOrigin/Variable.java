package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "VARIABLE", schema = "DPM_MD")
public class Variable implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "VARIABLEID")
    private int variableID;
    
    @Column(name = "TYPE")
    @NotNull
    @Size(max = 20)
    private String type;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getVariableID() {
        return variableID;
    }

    public void setVariableID(int variableID) {
        this.variableID = variableID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
}
