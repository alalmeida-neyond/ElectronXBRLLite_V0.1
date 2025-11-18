package com.example.demo.controller.Objects.Import;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.demo.controller.Objects.Entities.DPMOrigin.*;
import com.example.demo.Converter.LocalDateTimePersistenceConverter;
import com.example.demo.DTOs.*;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityResult;
import jakarta.persistence.FetchType;
import jakarta.persistence.FieldResult;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "IN_IMPORTEDVALUESTEMP", schema = "DPM_OD")
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "ValuesForOperationMapping",
            entities = {
                @EntityResult(entityClass = DataType.class),
                @EntityResult(entityClass = InImportKey.class, fields = {
            @FieldResult(column = "RowKeyID", name = "importKeyID"),
            @FieldResult(column = "RowKeyTypeID", name = "keyType")
        }),
                @EntityResult(entityClass = InImportKey.class, fields = {
            @FieldResult(column = "DesagregationCodeID", name = "importKeyID"),
            @FieldResult(column = "DesagregationCodeTypeID", name = "keyType")
        })
            },
            columns = {
            @ColumnResult(name = "NodeID", type = Integer.class),
            @ColumnResult(name = "Value", type = String.class),
            @ColumnResult(name = "X", type = Integer.class),
            @ColumnResult(name = "Y", type = Integer.class),
            @ColumnResult(name = "Z", type = Integer.class),
            @ColumnResult(name = "Type", type = String.class),
            @ColumnResult(name = "ValueID", type = Integer.class),
            @ColumnResult(name = "RefID", type = Integer.class),
            @ColumnResult(name = "ValueDomain", type = String.class)
        }
    ),
    @SqlResultSetMapping(
            name = "ImportedValuesRow",
            classes = {
                @ConstructorResult(
                        targetClass = ImportedValuesDTO.class,
                        columns = {
                            @ColumnResult(name = "module", type = String.class),
                            @ColumnResult(name = "table", type = String.class),
                            @ColumnResult(name = "referenceDate", type = String.class),
                            @ColumnResult(name = "entity", type = String.class),
                            @ColumnResult(name = "domain", type = String.class),
                            @ColumnResult(name = "linha", type = String.class),
                            @ColumnResult(name = "coluna", type = String.class),
                            @ColumnResult(name = "desagregationCode", type = String.class),
                            @ColumnResult(name = "rowKey", type = String.class),
                            @ColumnResult(name = "ruleValue", type = String.class),
                            @ColumnResult(name = "userId", type = String.class)
                        }
                )
            })
})
public class InImportedValuesTemp implements Serializable {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "impValuesSequenceTemp_gen")
    @SequenceGenerator(name = "impValuesSequenceTemp_gen", sequenceName = "DPM_OD.IMPVALUESTEMP_SEQ", allocationSize = 1)
    @Column(name = "IMPORTEDVALUESID")
    private int importedvaluesID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IMPORTEDTABLEID", referencedColumnName = "IMPORTEDTABLEID", nullable = false)
    private InImportedTablesTemp importedTableId;

    @Column(name = "IMPORTEDVALUE")
    private String importedValue;

    @Column(name = "RULEVALUE")
    private String ruleValue;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.DETACH)
    @JoinColumn(name = "IMPORTKEYID", referencedColumnName = "IMPORTKEYID", nullable = false)
    private InImportKey importKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VARIABLEVID", referencedColumnName = "VARIABLEVID", nullable = false)
    private VariableVersion variableVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CELLID", referencedColumnName = "CELLID", nullable = false)
    private Cell cell;
    
    @Column(name = "USERID")
    private String userid;

    @Column(name = "LASTCHANGE")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime lastChange;

    @Transient
    private String rowKeySASTransient;

    @Column(name = "LINHA")
    private String linha;

    @Column(name = "COLUNA")
    private String coluna;

    @Column(name = "SHEET")
    private String sheet;

    @Column(name = "ROWKEY")
    private String rowKey;

    public InImportedValuesTemp() {
    }

    public int getImportedvaluesID() {
        return importedvaluesID;
    }

    public void setImportedvaluesID(int importedvaluesID) {
        this.importedvaluesID = importedvaluesID;
    }

    public InImportedTablesTemp getImportedTableId() {
        return importedTableId;
    }

    public void setImportedTableId(InImportedTablesTemp importedTableId) {
        this.importedTableId = importedTableId;
    }

    public String getImportedValue() {
        return importedValue;
    }

    public void setImportedValue(String importedValue) {
        this.importedValue = importedValue;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }

    public InImportKey getImportKey() {
        return importKey;
    }

    public void setImportKey(InImportKey importKey) {
        this.importKey = importKey;
    }

    public VariableVersion getVariableVersion() {
        return variableVersion;
    }

    public void setVariableVersion(VariableVersion variableVersion) {
        this.variableVersion = variableVersion;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public LocalDateTime getLastChange() {
        return lastChange;
    }

    public void setLastChange(LocalDateTime lastChange) {
        this.lastChange = lastChange;
    }

    public String getRowKeySASTransient() {
        return rowKeySASTransient;
    }

    public void setRowKeySASTransient(String rowKeySASTransient) {
        this.rowKeySASTransient = rowKeySASTransient;
    }

    public String getLinha() {
        return linha;
    }

    public void setLinha(String linha) {
        this.linha = linha;
    }

    public String getColuna() {
        return coluna;
    }

    public void setColuna(String coluna) {
        this.coluna = coluna;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    
}
