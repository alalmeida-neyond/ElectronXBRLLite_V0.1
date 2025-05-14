/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import com.example.demo.controller.Objects.Entities.DataType;

/**
 *
 * @author njesus
 */
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
