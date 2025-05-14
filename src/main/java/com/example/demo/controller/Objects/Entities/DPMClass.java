package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "DPMCLASS")
public class DPMClass implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "CLASSID")
    private int classId;
    
    @Column(name = "NAME")
    @NotNull
    @Size(max = 50)
    private String name;
    
    @Column(name = "TYPE")
    @Size(max = 20)
    private String type;
    
    @JoinColumn(referencedColumnName = "CLASSID", name = "OWNERCLASSID")
    @ManyToOne(fetch = FetchType.LAZY)
    private DPMClass ownerClass;
    
    @Column(name = "HASREFERENCES", columnDefinition = "NUMBER(*,0)")
    @NotNull
    private boolean hasReferences;

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DPMClass getOwnerClass() {
        return ownerClass;
    }

    public void setOwnerClass(DPMClass ownerClass) {
        this.ownerClass = ownerClass;
    }

    public boolean isHasReferences() {
        return hasReferences;
    }

    public void setHasReferences(boolean hasReferences) {
        this.hasReferences = hasReferences;
    }
    
    
}
