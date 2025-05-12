package com.example.demo.controller.Objects.DAL;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.Entities.*;

public class VariableVersionDAL {

    /**
     * get the List of VariableVersion based in the moduleVID
     *
     * @param moduleVID
     * @return
     */
    public static List<VariableVersion> getListOfVariableVersionOfModuleSheets(int moduleVID) {
        JPA<VariableVersion> jpa = new JPA<VariableVersion>(VariableVersion.class);
        List<VariableVersion> listOfResults = new ArrayList<>();
        try {
            StringBuilder query = new StringBuilder("Select a.* from VariableVersion a ");
            query.append("inner join MODULEPARAMETERS b ");
            query.append("on a.VARIABLEVID = b.VARIABLEVID ");
            query.append("where b.MODULEVID = :moduleVID ");

            listOfResults = jpa.getTypedNativeResultList(query.toString(),
                    "moduleVID", moduleVID);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfResults;
    }

    public static VariableVersion getVariableVersionFromAbstract(int abstractId) {
        JPA<VariableVersion> jpa = new JPA<VariableVersion>(VariableVersion.class);
        List<VariableVersion> listOfResults = new ArrayList<>();
        VariableVersion returnResult = null;
        try {
            StringBuilder query = new StringBuilder("Select a.* from VariableVersion a ");
            query.append("inner JOIN tableversion b ");
            query.append("on a.code = b.code where b.tableid = :abstractId ");
            
            listOfResults = jpa.getTypedNativeResultList(query.toString(),
                    "abstractId", abstractId);
           if(!listOfResults.isEmpty()){
               returnResult = listOfResults.get(0);
           } 
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return returnResult;
    }

}
