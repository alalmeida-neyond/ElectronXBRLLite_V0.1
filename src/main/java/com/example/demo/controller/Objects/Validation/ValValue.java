package com.example.demo.controller.Objects.Validation;

import com.example.demo.controller.Objects.Entities.DPMOrigin.DataType;

public class ValValue {
    
    private DataType datatype;
    private String value;

    public ValValue(){}
    
    public ValValue(DataType datatype, String value){
        this.datatype = datatype;
        this.value = value;
    }
    
    public ValValue(String value){
        this.value = value;
    }

    public DataType getDatatype() {
        return datatype;
    }

    public void setDatatype(DataType datatype) {
        this.datatype = datatype;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
    
}
