package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "PROPERTY")
public class Property implements Serializable {

    @Id
    @NotNull
    @Column(name = "PROPERTYID")
    private int propertyID;
    

    
    @Column(name = "ISCOMPOSITE", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isComposite;
    
    @Column(name = "ISMETRIC", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isMetric;
    
    @JoinColumn(referencedColumnName = "DATATYPEID", name = "DATATYPEID")
    @ManyToOne
    private DataType dataType;
    
    @Column(name = "VALUELENGTH")
    private Double valueLength;
    
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

    public double getValueLength() {
        return valueLength;
    }

    public void setValueLength(double valueLength) {
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
