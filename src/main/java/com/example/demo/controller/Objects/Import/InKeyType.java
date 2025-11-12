package com.example.demo.controller.Objects.Import;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;


@Entity
@Table(name = "IN_KEYTYPE", schema = "DPM_ED")
public class InKeyType implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "KEYTYPEID")
    private int keyTypeID;
    
    @Column(name = "KEYTYPE")
    private String keyType;

    public InKeyType() {
    }
    
    public InKeyType(Integer keyTypeID){
        this.keyTypeID = keyTypeID;
    }

    public int getKeyTypeID() {
        return keyTypeID;
    }

    public void setKeyTypeID(int keyTypeID) {
        this.keyTypeID = keyTypeID;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
    
    
    
}
