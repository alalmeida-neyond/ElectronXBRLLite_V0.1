package com.example.demo.controller.Objects.IO;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = "IO_TYPESTATE", schema = "DPM_ED")
@NamedQuery(name = "IOTypeState.findAll", query = "SELECT e FROM IOTypeState e")
public class IOTypeState implements Serializable {

    @Id
    @NotNull
    @Column(name = "IO_TYPESTATEID")
    private int ioTypeStateId;

    @Column(name = "DESCRIPTION")
    private String description;

    public IOTypeState() {
    }

    public IOTypeState(int ioTypeStateId) {
        this.ioTypeStateId = ioTypeStateId;
    }
    
    public int getIoTypeStateId() {
        return ioTypeStateId;
    }

    public void setIoTypeStateId(int ioTypeStateId) {
        this.ioTypeStateId = ioTypeStateId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}