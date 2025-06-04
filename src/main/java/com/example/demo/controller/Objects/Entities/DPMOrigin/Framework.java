package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "FRAMEWORK")
public class Framework implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "FRAMEWORKID")
    private int frameworkId;
    
    @Column(name = "CODE")
    @NotNull
    @Size(max = 255)
    private String code;
    
    @Column(name = "NAME")
    @NotNull
    @Size(max = 255)
    private String name;
    
    @Column(name = "DESCRIPTION")
    @Size(max = 255)
    private String description;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getFrameworkId() {
        return frameworkId;
    }

    public void setFrameworkId(int frameworkId) {
        this.frameworkId = frameworkId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
    
}
