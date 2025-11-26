package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import org.eclipse.persistence.annotations.ReadOnly;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "CONTEXT", schema = "DPM_MD")
//@ReadOnly
public class Context implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "CONTEXTID")
    private int contextID;
    
    @Column(name = "SIGNATURE", unique = true)
    @NotNull
    @Size(max = 500)
    private String signature;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getContextID() {
        return contextID;
    }

    public void setContextID(int contextID) {
        this.contextID = contextID;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
    
}
