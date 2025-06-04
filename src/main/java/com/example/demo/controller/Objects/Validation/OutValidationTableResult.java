package com.example.demo.controller.Objects.Validation;

import java.io.Serializable;
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
@Table(name = "OUT_VALIDATIONTABLERESULT")
public class OutValidationTableResult implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VALIDATIONTABLERESULTID")
    @NotNull
    private Integer validationTableResultId;
    
    @JoinColumn(referencedColumnName = "VALIDATIONTABLEID", name = "VALIDATIONTABLEID")
    @ManyToOne
    private OutValidationTable outValidationTable;
    
    @JoinColumn(referencedColumnName = "VALIDATIONRESULTID", name = "VALIDATIONRESULTID")
    @ManyToOne
    private OutValidationResult outValidationResult;
    
    public OutValidationTableResult(){}
    
    public OutValidationTableResult(OutValidationTable outValidationTable, OutValidationResult outValidationResult){
        this.outValidationTable = outValidationTable;
        this.outValidationResult = outValidationResult;
    }

    public Integer getValidationTableResultId() {
        return validationTableResultId;
    }

    public void setValidationTableResultId(Integer validationTableResultId) {
        this.validationTableResultId = validationTableResultId;
    }

    public OutValidationTable getOutValidationTable() {
        return outValidationTable;
    }

    public void setOutValidationTable(OutValidationTable outValidationTable) {
        this.outValidationTable = outValidationTable;
    }

    public OutValidationResult getOutValidationResult() {
        return outValidationResult;
    }

    public void setOutValidationResult(OutValidationResult outValidationResult) {
        this.outValidationResult = outValidationResult;
    }
}
