package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import jakarta.persistence.*;
import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = "CONCEPT", schema = "DPM_MD")
public class Concept implements Serializable{
    
    @Id
    @Column(name = "CONCEPTGUID", columnDefinition = "RAW(50)", nullable = false)
    private byte[] conceptGUID;
    
    @JoinColumn(referencedColumnName = "CLASSID", name = "CLASSID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DPMClass dpmClass;
    
    @JoinColumn(referencedColumnName = "ORGID", name = "OWNERID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Organisation owner;

    public byte[] getConceptGUID() {
        return conceptGUID;
    }

    public void setConceptGUID(byte[] conceptGUID) {
        this.conceptGUID = conceptGUID;
    }

    public DPMClass getDpmClass() {
        return dpmClass;
    }

    public void setDpmClass(DPMClass dpmClass) {
        this.dpmClass = dpmClass;
    }

    public Organisation getOwner() {
        return owner;
    }

    public void setOwner(Organisation owner) {
        this.owner = owner;
    }
}
