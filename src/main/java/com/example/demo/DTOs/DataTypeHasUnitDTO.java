package com.example.demo.DTOs;

public class DataTypeHasUnitDTO {
    private int datatypeId;
    private boolean hasUnit;

    public DataTypeHasUnitDTO(int datatypeId, String hasUnit) {
        this.datatypeId = datatypeId;
        this.hasUnit = hasUnit!=null? hasUnit.equals("1") : false;
    }

    public int getDatatypeId() {
        return datatypeId;
    }

    public void setDatatypeId(int datatypeId) {
        this.datatypeId = datatypeId;
    }

    public boolean getHasUnit() {
        return hasUnit;
    }

    public void setHasUnit(boolean hasUnit) {
        this.hasUnit = hasUnit;
    }
    
    
}
