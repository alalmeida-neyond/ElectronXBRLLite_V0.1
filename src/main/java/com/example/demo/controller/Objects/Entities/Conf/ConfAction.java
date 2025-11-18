package com.example.demo.controller.Objects.Entities.Conf;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "CONF_ACTION", schema = "DPM_OD")
@NamedQuery(name="ConfAction.findAll", query="SELECT e FROM ConfAction e")
public class ConfAction implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "ACTIONID")
    private int actionId;
    
    @Column(name = "DESCRIPTION")
    private String description;

    public ConfAction() {
    }

    public ConfAction(int actionId) {
        this.actionId = actionId;
    }

    public ConfAction(String description) {
        this.description = description;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
