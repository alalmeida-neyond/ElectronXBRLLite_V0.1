package com.example.demo.Exception;

import org.apache.poi.ss.util.CellReference;

public class InvalidCellValueException extends Exception {

    private String sheetName;
    private String cell;
    private String invalidValue;
    
    public InvalidCellValueException(String sheetName, int row, int col, String invalidValue) {
        this(sheetName, new CellReference(row, col).formatAsString(), invalidValue);
    }

    public InvalidCellValueException(String sheetName, String cell, String invalidValue) {
        super(generateMessage(sheetName, cell, invalidValue));
            
        this.sheetName = sheetName;
        this.cell = cell;
        this.invalidValue = invalidValue;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getCell() {
        return cell;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    private static String generateMessage(String sheetName, String cell, String invalidValue) {
        return "In the sheet " 
                + sheetName
                + ", the cell "
                + cell
                + " contains an invalid value ("
                + invalidValue
                + ")";
    }

}
