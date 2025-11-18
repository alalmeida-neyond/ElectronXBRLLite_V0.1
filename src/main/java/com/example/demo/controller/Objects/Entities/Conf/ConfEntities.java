package com.example.demo.controller.Objects.Entities.Conf;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;


@Entity

@Table(name = "CONF_ENTITIES", schema = "DPM_OD")

@NamedQuery(name = "ConfEntities.findAll", query = "SELECT e FROM ConfEntities e")

public class ConfEntities implements Serializable {

    @Id
    @NotNull
    @Column(name = "ENTITYID")
    private int entityID;

    @NotNull
    @Column(name = "BDPID")
    private String bdpId;

    @Column(name = "LEICODE")
    private String leiCode;

    @Column(name = "DESCRIPTION")
    private String description;

    public ConfEntities() {
    }

    public ConfEntities(int entityID, String bdpId, String leiCode, String description) {
        this.entityID = entityID;
        this.bdpId = bdpId;
        this.leiCode = leiCode;
        this.description = description;
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    public String getBdpId() {
        return bdpId;
    }

    public void setBdpId(String bdpId) {
        this.bdpId = bdpId;
    }

    public String getLeiCode() {
        return leiCode;
    }

    public void setLeiCode(String leiCode) {
        this.leiCode = leiCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(entityID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConfEntities other = (ConfEntities) obj;
        return Objects.equals(this.getEntityID(), other.getEntityID());
    }
}
