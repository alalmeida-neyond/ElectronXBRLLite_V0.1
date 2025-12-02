package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.*;

import org.springframework.data.annotation.Immutable;

import com.example.demo.controller.Objects.Entities.DPMOrigin.IDs.KeyCompositionID;

@Entity
@Table(name = "KEYCOMPOSITION", schema = "DPM_MD")
public class KeyComposition implements Serializable{
    
    @EmbeddedId
    private KeyCompositionID keyCompositionID;

    @JoinColumn(name = "KEYID", referencedColumnName = "KEYID" ,nullable = false, insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private CompoundKey compoundKey;

    @JoinColumn(name = "VARIABLEVID", referencedColumnName = "VARIABLEVID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private VariableVersion variable;
    
    @Column(name = "ROWGUID", columnDefinition = "RAW(50)")
    private UUID rowGUID;

    public KeyCompositionID getKeyCompositionId() {
        return keyCompositionID;
    }

    public void setKeyCompositionId(KeyCompositionID keyCompositionID) {
        this.keyCompositionID = keyCompositionID;
    }

    public CompoundKey getCompoundKey() {
        return compoundKey;
    }

    public void setCompoundKey(CompoundKey compoundKey) {
        this.compoundKey = compoundKey;
    }

    public VariableVersion getVariable() {
        return variable;
    }

    public void setVariable(VariableVersion variable) {
        this.variable = variable;
    }

    public UUID getRowGUID() {
        return rowGUID;
    }

    public void setRowGUID(UUID rowGUID) {
        this.rowGUID = rowGUID;
    }
}
