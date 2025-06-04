/*

* Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license

* Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template

 */
package com.example.demo.controller.Objects.Entities.Conf;

import java.io.Serializable;

import jakarta.persistence.Column;

import jakarta.persistence.Id;

import jakarta.persistence.Table;

import jakarta.persistence.Entity;

import jakarta.persistence.NamedQuery;

import jakarta.validation.constraints.NotNull;


@Entity

@Table(name = "CONF_PERIODICITY")

@NamedQuery(name = "ConfPeriodicity.findAll", query = "SELECT e FROM ConfPeriodicity e")

public class ConfPeriodicity implements Serializable {

    @Id
    @NotNull
    @Column(name = "PERIODICITYID")
    private int periodicityId;

    @NotNull
    @Column(name = "DURATION")
    private int duration;

    @NotNull
    @Column(name = "DESCRIPTION")
    private String description;

    public ConfPeriodicity() {
    }

    public int getPeriodicityId() {
        return periodicityId;
    }

    public void setPeriodicityId(int periodicityId) {
        this.periodicityId = periodicityId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
