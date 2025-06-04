/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Lock;

import java.io.Serializable;

import com.example.demo.controller.Objects.IO.IO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "LOCKASSOCIATION")
public class LockAssociation implements Serializable {

    @Id
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "LOCKASSOCIATIONID")
    private int lockAssociationId;

    @ManyToOne
    @JoinColumn(name = "IOIDLOCK", referencedColumnName = "IOID", nullable = false)
    private IO ioIdLock;

    @ManyToOne
    @JoinColumn(name = "IOIDVALIDATE", referencedColumnName = "IOID")
    private IO ioIdValidate;

    @ManyToOne
    @JoinColumn(name = "IOIDGENERATE", referencedColumnName = "IOID", nullable = false)
    private IO ioIdGenerate;

    public LockAssociation() {
    }

    public LockAssociation(IO ioIdLock, IO ioIdValidate, IO ioIdGenerate) {
        this.ioIdLock = ioIdLock;
        this.ioIdValidate = ioIdValidate;
        this.ioIdGenerate = ioIdGenerate;
    }

    public int getLockAssociationId() {
        return lockAssociationId;
    }

    public void setLockAssociationId(int lockAssociationId) {
        this.lockAssociationId = lockAssociationId;
    }

    public IO getIoIdLock() {
        return ioIdLock;
    }

    public void setIoIdLock(IO ioIdLock) {
        this.ioIdLock = ioIdLock;
    }

    public IO getIoIdValidate() {
        return ioIdValidate;
    }

    public void setIoIdValidate(IO ioIdValidate) {
        this.ioIdValidate = ioIdValidate;
    }

    public IO getIoIdGenerate() {
        return ioIdGenerate;
    }

    public void setIoIdGenerate(IO ioIdGenerate) {
        this.ioIdGenerate = ioIdGenerate;
    }

}
