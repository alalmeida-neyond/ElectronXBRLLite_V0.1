package com.example.demo.DTOs;

import com.example.demo.Data.Connection;
import com.example.demo.controller.Objects.Import.InImportKey;
import com.example.demo.controller.Objects.Import.InKeyType;
import jakarta.persistence.EntityManager;

public class DesagregationImportKeyDTO {
    private Integer importKeyID;
    private Integer importKeyTypeID;


    public DesagregationImportKeyDTO(Integer DesagregationCodeID, Integer DesagregationCodeTypeID){
        this.importKeyID = DesagregationCodeID;
        this.importKeyTypeID = DesagregationCodeTypeID;
    }

    public Integer getImportKeyID(){
        return this.importKeyID;
    }

    
    public Integer getImportKeyTypeID(){
        return this.importKeyTypeID;
    }

    public void setImportKeyID(Integer importKeyID){
        this.importKeyID =  importKeyID;
    }
    public void setImportKeyTypeID(Integer importKeyTypeID){
        this.importKeyTypeID =  importKeyTypeID;
    }

    /**
     * Eagerly loads the InImportKey and its properties with the entity manager find function
     * @return InImportKey the loaded entity or null if no entity is found with importKeyID
     */
    public InImportKey convertToImportKey(){
        EntityManager entityManager = Connection.getEm();
        InImportKey key = entityManager.find(InImportKey.class, importKeyID);
        key.setKeyType(new InKeyType(importKeyTypeID));
        return key;
    }
}
