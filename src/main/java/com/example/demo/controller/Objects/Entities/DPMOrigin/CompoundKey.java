package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = "COMPOUNDKEY", schema = "DPM_MD")
public class CompoundKey implements Serializable {
    
    @Id
    @NotNull
    @Column(name = "KEYID")
    private int keyId;
    
    @Column(name = "SIGNATURE", unique = true)
    @NotNull
    @Size(max = 255)
    private String signature;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
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