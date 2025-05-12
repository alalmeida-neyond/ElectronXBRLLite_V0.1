package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.DTOs.CellVariableDTO;
import com.example.demo.DTOs.HeaderDTO;

public class CellVariableService {
    
    //Gets the CellVariableService and removes the one that already exists;
    public static CellVariableDTO getCellVariableServiceDTOFromList(List<CellVariableDTO> listCellVariable,HeaderDTO row,HeaderDTO collumn ,HeaderDTO sheet, boolean opeanRow){
       CellVariableDTO result = null;
        try {
            Optional<CellVariableDTO> possibleResult = listCellVariable.stream()
                .filter(cellVariable -> (((cellVariable.getRowId() == 0 && row == null)|| cellVariable.getRowId() == row.getHeaderId()) && ( (cellVariable.getCollumnId() == 0 && collumn == null)|| cellVariable.getCollumnId()== collumn.getHeaderId()) && ( (cellVariable.getSheetId() == 0 && sheet == null)|| cellVariable.getSheetId()== sheet.getHeaderId())))
                .findFirst();
             result =possibleResult.orElse(null);
            if(!opeanRow){
                listCellVariable.remove(result);
            }
       }
       catch(Exception e){
           //If enter here grey SPOT in report (not to fill)
            //e.printStackTrace();
       }
        return result;
    }
}