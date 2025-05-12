/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.example.demo.controller.Objects.DPMOrigin;

import java.io.Serializable;

import com.example.demo.controller.Objects.Entities.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "HEADER")
public class Header implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "HEADERID")
    private int headerId;
    
    @ManyToOne
    @JoinColumn(name = "TABLEID", referencedColumnName = "TABLEID", nullable = false)
    private TableDPM table;
    
    @Column(name = "DIRECTION")
    @NotNull
    @Size(max = 1)
    private char direction;
    
    @Column(name = "ISKEY", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isKey;
    
    @Column(name = "ISATTRIBUTE", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isAttribute;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne
    private Concept concept;

    public int getHeaderId() {
        return headerId;
    }

    public void setHeaderId(int headerId) {
        this.headerId = headerId;
    }

    public TableDPM getTable() {
        return table;
    }

    public void setTable(TableDPM table) {
        this.table = table;
    }

    public char getDirection() {
        return direction;
    }

    public void setDirection(char direction) {
        this.direction = direction;
    }

    public boolean isIsKey() {
        return isKey;
    }

    public void setIsKey(boolean isKey) {
        this.isKey = isKey;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public boolean isIsAttribute() {
        return isAttribute;
    }

    public void setIsAttribute(boolean isAttribute) {
        this.isAttribute = isAttribute;
    }
}
