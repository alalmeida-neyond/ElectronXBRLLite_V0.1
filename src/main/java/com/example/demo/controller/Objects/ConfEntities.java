package com.example.demo.controller.Objects;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;


@Entity

@Table(name = "CONF_ENTITIES")

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

    public ConfEntities(int entityID, String Id, String leiCode, String description) {
        this.entityID = entityID;
        this.bdpId = Id;
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

    public void setId(String Id) {
        this.bdpId = Id;
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
}
