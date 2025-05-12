package com.example.demo.controller.Objects.Import;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;


@Entity
@Table(name = "IN_KEYASSOCIATION")
public class InKeyAssociation implements Serializable {
    
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "KEYASSOCIATION_GEN", sequenceName = "IN_KEYASSOCIATION_SEQ", allocationSize = 1)
    @Column(name = "KEYASSOCIATIONID")
    private int keyAssociationID;
    
    @NotNull
    @Column(name = "PROPERTYNAME")
    private String propertyName;
    
    @NotNull
    @Column(name = "PROPERTYVALUE")
    private String propertyValue;
    
    @JoinColumn(name = "IMPORTKEYID", referencedColumnName = "IMPORTKEYID")
    @ManyToOne
    private InImportKey importedKey;

    public int getKeyAssociationID() {
        return keyAssociationID;
    }

    public void setKeyAssociationID(int keyAssociationID) {
        this.keyAssociationID = keyAssociationID;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public InImportKey getImportedKey() {
        return importedKey;
    }

    public void setImportedKey(InImportKey importedKey) {
        this.importedKey = importedKey;
    }
}
