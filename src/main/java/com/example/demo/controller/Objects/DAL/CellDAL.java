package com.example.demo.controller.Objects.DAL;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.DTOs.CellVariableDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.Entities.TableVersionDPM;


public class CellDAL {

     public static List<CellVariableDTO> getListOfCellVariable(TableVersionDPM tableVersion){
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> listTemp = new ArrayList<>();
        List<CellVariableDTO> listOfModules = new ArrayList<>();
        try {                     
            StringBuilder query = new StringBuilder("Select tbce.variablevid, ce.\"RowID\",  ce.columnid,  ce.sheetid , ce.cellid ");
            query.append("From Cell ce ");
            query.append("inner join TableVersionCell tbce on ce.cellid = tbce.cellid ");
            query.append("where tbce.tablevid = :tablevid and ce.tableid = :tableid ");
            
            listTemp = jpa.getNativeResultList(query.toString(),
                    "tablevid", tableVersion.getTableVID(),
                    "tableid", tableVersion.getTable().getTableId());
            for(Object[] obj: listTemp){
                listOfModules.add(new CellVariableDTO(obj[0] != null ? Integer.valueOf(obj[0].toString()):0,obj[1] != null ?Integer.valueOf(obj[1].toString()):0,obj[2] != null ? Integer.valueOf(obj[2].toString()) : 0,obj[3] != null ? Integer.valueOf(obj[3].toString()) : 0,obj[4] != null ?Integer.valueOf(obj[4].toString()):0));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            jpa.close();
        }
        return listOfModules;
    }

}
