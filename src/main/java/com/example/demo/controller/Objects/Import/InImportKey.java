package com.example.demo.controller.Objects.Import;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "IN_IMPORTKEY")
public class InImportKey implements Serializable {
    
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "IMPORTKEY_GEN", sequenceName = "IN_IMPORTKEY_SEQ", allocationSize = 1)
    @Column(name = "IMPORTKEYID")
    private int importKeyID;
    
    @JoinColumn(name = "KEYTYPEID", referencedColumnName = "KEYTYPEID")
    @ManyToOne
    private InKeyType keyType;
    
    //@OneToMany(cascade = CascadeType.ALL, mappedBy = "importedKey")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "importedKey")
    private List<InKeyAssociation> listPropertyValues;

    public InImportKey() {
    }

    public InImportKey(InKeyType keyType) {
        this.keyType = keyType;
    }
    
    public int getImportKeyID() {
        return importKeyID;
    }

    public void setImportKeyID(int importKeyID) {
        this.importKeyID = importKeyID;
    }

    public InKeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(InKeyType keyType) {
        this.keyType = keyType;
    }

    public List<InKeyAssociation> getListPropertyValues() {
        return listPropertyValues;
    }

    public void setListPropertyValues(List<InKeyAssociation> listPropertyValues) {
        this.listPropertyValues = listPropertyValues;
    }
}
