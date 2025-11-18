package com.example.demo.controller.Objects.Entities.DPMOrigin.IDs;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;



@Embeddable
public class KeyCompositionID implements Serializable{
    
    @Column(name = "KEYID")
    private int keyID;
    
    @Column(name = "VARIABLEVID")
    private int variableVID;

    public int getKeyID() {
        return keyID;
    }

    public void setKeyID(int keyID) {
        this.keyID = keyID;
    }

    public int getVariableVID() {
        return variableVID;
    }

    public void setVariableVID(int variableVID) {
        this.variableVID = variableVID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyCompositionID)) return false;
        KeyCompositionID that = (KeyCompositionID) o;
        return keyID == that.keyID &&
               variableVID == that.variableVID;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(keyID, variableVID);
    }
}
