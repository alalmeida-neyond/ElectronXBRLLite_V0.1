/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Logs;

import java.time.LocalDateTime;

import com.example.demo.Converter.LocalDateTimePersistenceConverter;
import com.example.demo.controller.Objects.Generation.OutXBRLGenerated;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "GENERATELOG")
public class GenerateLog {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@SequenceGenerator(name = "genLogSequence_gen", sequenceName = "DPM_ED.GENERATELOG_SEQ", allocationSize = 1)
    @Column(name = "ID_GenerateSTATUS")
    private int idGenerateLog;

    @Column(name = "LOG_DESCRIPTION")
    private String description;

    @Column(name = "TIMESTAMPCREATED")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime timeStampCreated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "XBRL_ID", referencedColumnName = "XBRL_ID", nullable = false)
    private OutXBRLGenerated xbrlGenerates;

    public GenerateLog() {
    }

    public int getIdGenerateLog() {
        return idGenerateLog;
    }

    public void setIdGenerateLog(int idGenerateLog) {
        this.idGenerateLog = idGenerateLog;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimeStampCreated() {
        return timeStampCreated;
    }

    public void setTimeStampCreated(LocalDateTime timeStampCreated) {
        this.timeStampCreated = timeStampCreated;
    }

    public OutXBRLGenerated getXbrlGenerates() {
        return xbrlGenerates;
    }

    public void setXbrlGenerates(OutXBRLGenerated xbrlGenerates) {
        this.xbrlGenerates = xbrlGenerates;
    }

}
