package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "\"TABLE\"")
@SqlResultSetMapping(
    name = "TableDPMMapping",
    entities = {
        @EntityResult(entityClass = TableDPM.class )}
)
public class TableDPM implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "TABLEID")
    private int tableId;
    
    @Column(name = "ISABSTRACT", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isAbstract;
    
    @Column(name = "HASOPENCOLUMNS", columnDefinition = "CHAR(1)", nullable = false)
    private boolean hasOpenColumns;
            
    @Column(name = "HASOPENROWS", columnDefinition = "CHAR(1)", nullable = false)
    private boolean hasOpenRows;
    
    @Column(name = "HASOPENSHEETS", columnDefinition = "CHAR(1)", nullable = false)
    private boolean hasOpenSheets;
    
    @Column(name = "ISNORMALISED", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isNormalised;
    
    @Column(name = "ISFLAT", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isFlat;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public boolean isIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public boolean isHasOpenColumns() {
        return hasOpenColumns;
    }

    public void setHasOpenColumns(boolean hasOpenColumns) {
        this.hasOpenColumns = hasOpenColumns;
    }

    public boolean isHasOpenRows() {
        return hasOpenRows;
    }

    public void setHasOpenRows(boolean hasOpenRows) {
        this.hasOpenRows = hasOpenRows;
    }

    public boolean isHasOpenSheets() {
        return hasOpenSheets;
    }

    public void setHasOpenSheets(boolean hasOpenSheets) {
        this.hasOpenSheets = hasOpenSheets;
    }

    public boolean isIsNormalised() {
        return isNormalised;
    }

    public void setIsNormalised(boolean isNormalised) {
        this.isNormalised = isNormalised;
    }

    public boolean isIsFlat() {
        return isFlat;
    }

    public void setIsFlat(boolean isFlat) {
        this.isFlat = isFlat;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
    
}
