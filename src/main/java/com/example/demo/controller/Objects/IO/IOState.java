package com.example.demo.controller.Objects.IO;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "IO_STATE")
@NamedQuery(name = "IOState.findAll", query = "SELECT e FROM IOState e")
public class IOState implements Serializable {

    @Id
    @NotNull
    @Column(name = "IO_STATEID")
    private int ioStateId;

    @ManyToOne
    @JoinColumn(name = "IO_TYPESTATEID", referencedColumnName = "IO_TYPESTATEID", nullable = false)
    private IOTypeState ioTypeStateId;

    @Column(name = "DESCRIPTION")
    private String description;

    public IOState() {
    }

    public IOState(int ioStateId, IOTypeState ioTypeStateId) {
        this.ioStateId = ioStateId;
        this.ioTypeStateId = ioTypeStateId;
    }

    public int getIoStateId() {
        return ioStateId;
    }

    public void setIoStateId(int ioStateId) {
        this.ioStateId = ioStateId;
    }

    public IOTypeState getIoTypeStateId() {
        return ioTypeStateId;
    }

    public void setIoTypeStateId(IOTypeState ioTypeStateId) {
        this.ioTypeStateId = ioTypeStateId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    

}
