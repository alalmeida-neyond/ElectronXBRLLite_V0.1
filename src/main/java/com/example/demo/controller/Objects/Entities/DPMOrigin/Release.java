package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import com.example.demo.Converter.*;
import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "RELEASE")
public class Release implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "RELEASEID")
    private int releaseID;
    
    @Column(name = "CODE")
    @NotNull
    @Size(max = 20)
    private String code;
    
    @Column(name = "\"Date\"")
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate date;
    
    @Column(name = "DESCRIPTION")
    @Size(max = 255)
    private String description;
    
    @Column(name = "STATUS")
    @Size(max = 50)
    private String status;
    
    @Column(name = "ISCURRENT", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isCurrent;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;
    
    @Column(name = "LATESTVARIABLEGENTIME")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime latestVariableGenTime;

    public int getReleaseID() {
        return releaseID;
    }

    public void setReleaseID(int releaseID) {
        this.releaseID = releaseID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public LocalDateTime getLatestVariableGenTime() {
        return latestVariableGenTime;
    }

    public void setLatestVariableGenTime(LocalDateTime latestVariableGenTime) {
        this.latestVariableGenTime = latestVariableGenTime;
    }
    
    
}
