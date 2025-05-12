/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.DTOs;

/**
 *
 * @author rmaria
 */
public class CellVariableDTO {
    private int collumnId;
    private int rowId;
    private int sheetId;
    private int variableVid;
    private int cellId;

    public CellVariableDTO(Integer variableVid, Integer rowId, Integer collumnId, Integer sheetId, Integer cellId) {
        this.collumnId = (collumnId  == null ? 0 : collumnId);
        this.rowId = (rowId  == null ? 0 : rowId);
        this.sheetId = (sheetId == null ? 0 : sheetId);
        this.variableVid = (variableVid == null ? 0 : variableVid);
        this.cellId = (cellId == null ? 0 : cellId);
    }
    
    public CellVariableDTO() {
    } 
    
    public int getCollumnId() {
        return collumnId;
    }

    public void setCollumnId(int collumnId) {
        this.collumnId = collumnId;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public int getSheetId() {
        return sheetId;
    }

    public void setSheetId(int sheetId) {
        this.sheetId = sheetId;
    }

    public int getVariableVid() {
        return variableVid;
    }

    public void setVariableVid(int variableVid) {
        this.variableVid = variableVid;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }
     
}
