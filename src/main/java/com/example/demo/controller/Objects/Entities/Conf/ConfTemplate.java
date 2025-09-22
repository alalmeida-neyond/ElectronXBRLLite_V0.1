package com.example.demo.controller.Objects.Entities.Conf;

import java.io.Serializable;

import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "CONF_TEMPLATE")
public class ConfTemplate implements Serializable {
    
    @Id
    @NotNull
    @Column(name = "templateID")
    private int templateID;
    
    @Column(name = "Filename")
    private String filename;
    
    @Column(name = "ServerFilename")
    private String serverFilename;

    @Column(name = "EntryPointURL")
    private String entryPointURL;
    
    @Column(name = "JSONFileName")
    private String JSONFileName;
    
    
    @ManyToOne
    @JoinColumn(name = "templateID", referencedColumnName = "MODULEVID", nullable = false, insertable = false, updatable = false)
    private ModuleVersion moduleVersion;

    @Transient
    private boolean editMode;
    
    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
    
    public int getTemplateID() {
        return templateID;
    }

    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getServerFilename() {
        return serverFilename;
    }

    public void setServerFilename(String serverFilename) {
        this.serverFilename = serverFilename;
    }

    public String getEntryPointURL() {
        return entryPointURL;
    }

    public void setEntryPointURL(String entryPointURL) {
        this.entryPointURL = entryPointURL;
    }

    public String getJSONFileName() {
        return JSONFileName;
    }

    public void setJSONFileName(String JSONFileName) {
        this.JSONFileName = JSONFileName;
    }

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

}
