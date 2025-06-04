package com.example.demo.controller.Objects.Entities.Conf;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;


@Entity
@Table(name = "CONF_APPCONFIGS")
@NamedQuery(name="ConfAppConfigs.findAll", query="SELECT a FROM ConfAppConfigs a")
public class ConfAppConfigs implements Serializable {
    
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "confAppConfigs_gen", sequenceName = "CONF_APPCONFIGS_SEQ", allocationSize = 1)
    @Column(name = "APPCONFIGID")
    private int appConfigId;
    
    @NotNull
    @Column(name = "CONFIGKEY")
    private String key;
    
    @Column(name = "CONFIGVALUE")
    private String value;

    public ConfAppConfigs() {
    }

    public ConfAppConfigs(int appConfigId, String key, String value) {
        this.appConfigId = appConfigId;
        this.key = key;
        this.value = value;
    }

    public int getAppConfigId() {
        return appConfigId;
    }

    public void setAppConfigId(int appConfigId) {
        this.appConfigId = appConfigId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
     
}
