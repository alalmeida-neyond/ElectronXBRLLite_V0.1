package com.example.demo.controller.Objects.Entities.DAL;

import com.example.demo.Data.Access.JPA;


public class PropertyDAL {     
     
    public static int getListOfCellVariable(int variableVid){
        JPA<Integer> jpa = new JPA<Integer>(Integer.class);
        Integer result = null;
        try {
            StringBuilder query = new StringBuilder("Select a.datatypeid from Property a ");
            query.append("inner join VariableVersion b on a.propertyid = b.propertyid ");
            query.append("where b.variablevid = :variablevid ");            
            
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
