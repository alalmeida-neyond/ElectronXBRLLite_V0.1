package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.eclipse.persistence.annotations.ReadOnly;
import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = "PROPERTY", schema = "DPM_MD")
//@ReadOnly
public class Property implements Serializable {

    @Id
    @NotNull
    @Column(name = "PROPERTYID")
    private int propertyID;
    
//    @JoinColumn(referencedColumnName = "ITEMID", name = "PROPERTYID")
//    @OneToOne
//    private Item item;
    
    @Column(name = "ISCOMPOSITE", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isComposite;
    
    @Column(name = "ISMETRIC", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isMetric;
    
    @JoinColumn(referencedColumnName = "DATATYPEID", name = "DATATYPEID")
    @ManyToOne
    private DataType dataType;
    
    @Column(name = "VALUELENGTH")
    private Integer valueLength;
    
    @Column(name = "PERIODTYPE")
    @Size(max = 20)
    private String periodType;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public boolean isIsComposite() {
        return isComposite;
    }

    public void setIsComposite(boolean isComposite) {
        this.isComposite = isComposite;
    }

    public boolean isIsMetric() {
        return isMetric;
    }

    public void setIsMetric(boolean isMetric) {
        this.isMetric = isMetric;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Integer getValueLength() {
        return valueLength;
    }

    public void setValueLength(Integer valueLength) {
        this.valueLength = valueLength;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public int getPropertyID() {
        return propertyID;
    }

    public void setPropertyID(int propertyID) {
        this.propertyID = propertyID;
    }

}
