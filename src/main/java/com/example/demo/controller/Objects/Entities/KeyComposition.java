package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "KEYCOMPOSITION")
public class KeyComposition implements Serializable{
    
    @Id
    private KeyCompositionID keyCompositionID;
    
    @MapsId("keyID")
    //@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ManyToOne(optional = false)
    @JoinColumn(referencedColumnName = "KEYID", name = "KEYID", insertable = false, updatable = false)
    private CompoundKey compoundKey;
    
    @MapsId("variableVID")
    //@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ManyToOne(optional = false)
    @JoinColumn(referencedColumnName = "VARIABLEVID", name = "VARIABLEVID", insertable = false, updatable = false)
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
