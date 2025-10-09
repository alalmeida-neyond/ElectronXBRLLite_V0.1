package com.example.demo.DTOs;

public class KeyAssociationDTO {

    private String propertyName;
    
    private String propertyValue;
    
    private String propertyOriginalValue;

    public KeyAssociationDTO(String propertyName, String propertyValue, String propertyOriginalValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.propertyOriginalValue = propertyOriginalValue;
    }
    
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyOriginalValue() {
        return propertyOriginalValue;
    }

    public void setPropertyOriginalValue(String propertyOriginalValue) {
        this.propertyOriginalValue = propertyOriginalValue;
    }   
}
