
package com.example.demo.controller.Objects.Entities.DAL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.DTOs.KeyAssociationDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.Import.*;

import jakarta.persistence.NoResultException;

public class InKeyAssociationDAL {

    public static Map<Integer, List<InKeyAssociation>> getListOfKeyAssociationsBasedOnImportKey(List<InImportKey> listImp) {
        
        if (listImp.isEmpty() || listImp == null || listImp.get(0) == null) {
            return new HashMap<Integer, List<InKeyAssociation>>();
        }
        List<InKeyAssociation> queryResult = new ArrayList<InKeyAssociation>();
        Map<Integer, List<InKeyAssociation>> finalResult = new HashMap<Integer, List<InKeyAssociation>>();
        JPA<InKeyAssociation> jpa = new JPA<InKeyAssociation>(InKeyAssociation.class);

        
        StringBuilder queryString = new StringBuilder("Select IN_KEYASSOCIATION.* from DPM_OD.IN_KEYASSOCIATION ");
        queryString.append(" where ");
      
        int maxElementsInsideIn = 1000;
        for(int i = 0; i < listImp.size(); i+=maxElementsInsideIn){
            int maxIndOfCurrentList = Math.min(i+maxElementsInsideIn, listImp.size());
            queryString.append(" IMPORTKEYID in (  ");
            for(int j = i; j < maxIndOfCurrentList;j++){
                queryString.append("?keyid").append(j);
                if(j < maxIndOfCurrentList - 1){
                    queryString.append(", ");
                }
            }
            queryString.append(" ) ");
            if(maxIndOfCurrentList < listImp.size()){
                queryString.append(" or ");
            }
        }

        try {
            queryResult = jpa.getTypedNativeResultListForAListParameter(queryString.toString(), "keyid", listImp);
        } catch (NoResultException nrex) {
            nrex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        for(InKeyAssociation key : queryResult){
            if(finalResult.containsKey(key.getImportedKey().getImportKeyID())){
               finalResult.get(key.getImportedKey().getImportKeyID()).add(key);
            }else{
              List<InKeyAssociation> tempList = new ArrayList<InKeyAssociation>();
              tempList.add(key);
              finalResult.put(key.getImportedKey().getImportKeyID(),tempList);
            }
        }
        return finalResult;
        
    }
    
}
