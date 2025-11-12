package com.example.demo.controller.Objects.Import;

import java.io.Serializable;

import com.example.demo.DTOs.KeyAssociationDTO;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;


@SqlResultSetMapping(
        name="KeyAssociationDTO",
        classes = {
            @ConstructorResult(
                    targetClass = KeyAssociationDTO.class,
                    columns = {
                        @ColumnResult(name = "propertyName", type = String.class),
                        @ColumnResult(name = "propertyValue", type = String.class),
                        @ColumnResult(name = "propertyOriginalValue", type = String.class)
                    }
            )
        }
)
@Entity
@Table(name = "IN_KEYASSOCIATION", schema = "DPM_ED")
public class InKeyAssociation implements Serializable {
    
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "KEYASSOCIATION_GEN")
    @SequenceGenerator(name = "KEYASSOCIATION_GEN", sequenceName = "DPM_ED.IN_KEYASSOCIATION_SEQ", allocationSize = 1)
    @Column(name = "KEYASSOCIATIONID")
    private int keyAssociationID;
    
    @NotNull
    @Column(name = "PROPERTYNAME")
    private String propertyName;
    
    @NotNull
    @Column(name = "PROPERTYVALUE")
    private String propertyValue;
    
    @Column(name = "PROPERTYORIGINALVALUE")
    private String propertyOriginalValue;
    
    @JoinColumn(name = "IMPORTKEYID", referencedColumnName = "IMPORTKEYID")
    @ManyToOne(fetch = FetchType.LAZY)
    private InImportKey importedKey;

    public InKeyAssociation(String propertyName, String propertyValue, String propertyOriginalValue, InImportKey importedKey) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.propertyOriginalValue = propertyOriginalValue;
        this.importedKey = importedKey;
    }
    
    public InKeyAssociation(String propertyName, String propertyValue, String propertyOriginalValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.propertyOriginalValue = propertyOriginalValue;
    }
    
    public InKeyAssociation(){}

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

    public String getPropertyOriginalValue() {
        return propertyOriginalValue;
    }

    public void setPropertyOriginalValue(String propertyOriginalValue) {
        this.propertyOriginalValue = propertyOriginalValue;
    }
    
    
}
