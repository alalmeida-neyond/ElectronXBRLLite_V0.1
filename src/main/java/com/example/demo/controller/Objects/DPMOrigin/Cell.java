/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.example.demo.controller.Objects.DPMOrigin;

import java.io.Serializable;

import com.example.demo.controller.Objects.Entities.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "CELL")
public class Cell implements Serializable {

    @Id
    @NotNull
    @Column(name = "CELLID")
    private int cellId;

    @ManyToOne
    @JoinColumn(name = "TABLEID", referencedColumnName = "TABLEID", nullable = false)
    private TableDPM table;

    @JoinColumn(referencedColumnName = "HEADERID", name = "COLUMNID", nullable = false)
    @ManyToOne
    private Header column;

    @JoinColumn(referencedColumnName = "HEADERID", name = "RowID", nullable = true)
    @ManyToOne
    private Header row;

    @JoinColumn(referencedColumnName = "HEADERID", name = "SHEETID", nullable = true)
    @ManyToOne
    private Header sheet;

    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public TableDPM getTable() {
        return table;
    }

    public void setTable(TableDPM table) {
        this.table = table;
    }

    public Header getColumn() {
        return column;
    }

    public void setColumn(Header column) {
        this.column = column;
    }

    public Header getRow() {
        return row;
    }

    public void setRow(Header row) {
        this.row = row;
    }

    public Header getSheet() {
        return sheet;
    }

    public void setSheet(Header sheet) {
        this.sheet = sheet;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
