package com.example.demo.controller.Objects.Entities.DAL;

import java.math.BigDecimal;

import com.example.demo.Data.Access.JPA;


public class PropertyDAL {     
     
    public static int getListOfCellVariable(int variableVid){
        JPA<BigDecimal> jpa = new JPA<BigDecimal>(BigDecimal.class);
        BigDecimal result = null;
        try {
            StringBuilder query = new StringBuilder("Select a.datatypeid from DPM_MD.Property a ");
            query.append("inner join DPM_MD.VariableVersion b on a.propertyid = b.propertyid ");
            query.append("where b.variablevid = ?variablevid ");             
            
            result = jpa.getTypedNativeResult(query.toString(),
                    "variablevid", variableVid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            jpa.close();
        }
        return result == null ? 0 : result.intValue();
    } 

}
