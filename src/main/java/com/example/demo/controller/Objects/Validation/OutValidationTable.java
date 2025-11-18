package com.example.demo.controller.Objects.Validation;

import java.io.Serializable;

import com.example.demo.DTOs.ValidationResumesDTO;
import com.example.demo.controller.Objects.Entities.DPMOrigin.TableVersionDPM;
import com.example.demo.controller.Objects.IO.IO;
import com.example.demo.controller.Objects.IO.IOState;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "OUT_VALIDATIONTABLE", schema = "DPM_OD")
@SqlResultSetMapping(
    name = "ValidationsResumes",
    classes = {
        @ConstructorResult(
                targetClass = ValidationResumesDTO.class,
                columns = {
                    @ColumnResult(name = "module", type = String.class),
                    @ColumnResult(name = "entity", type = String.class),
                    @ColumnResult(name = "domain", type = String.class),
                    @ColumnResult(name = "referenceDate", type = String.class),
                    @ColumnResult(name = "tablecode", type = String.class),
                    @ColumnResult(name = "desagCode", type = String.class),
                    @ColumnResult(name = "isValidated", type = Integer.class),
                }
        )
})
public class OutValidationTable implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OUT_VALIDATIONTABLE_SEQ")
    @SequenceGenerator(name = "OUT_VALIDATIONTABLE_SEQ", sequenceName = "DPM_OD.OUT_VALIDATIONTABLE_SEQ", allocationSize = 1)
    @Column(name = "VALIDATIONTABLEID")
    @NotNull
    private Integer validationTableId;
    
    @JoinColumn(referencedColumnName = "TABLEVID", name = "TABLEVID")
    @ManyToOne
    private TableVersionDPM tableVersion;
    
    @JoinColumn(referencedColumnName = "IOID", name = "IOID")
    @ManyToOne
    private IO io;
    
    @JoinColumn(referencedColumnName = "IO_STATEID", name = "STATEID")
    @ManyToOne
    private IOState ioState;
    
    public OutValidationTable(){}
    
    public OutValidationTable(TableVersionDPM table, IO io, IOState ioState){
        this.tableVersion = table;
        this.io = io;
        this.ioState = ioState;
    }

    public Integer getValidationTableId() {
        return validationTableId;
    }

    public void setValidationTableId(Integer validationTableId) {
        this.validationTableId = validationTableId;
    }

    public TableVersionDPM getInImportedTable() {
        return tableVersion;
    }

    public void setInImportedTable(TableVersionDPM table) {
        this.tableVersion = table;
    }

    public IO getIo() {
        return io;
    }

    public void setIo(IO io) {
        this.io = io;
    }

    public IOState getIoState() {
        return ioState;
    }

    public void setIoState(IOState ioState) {
        this.ioState = ioState;
    }
    
    
    
}
