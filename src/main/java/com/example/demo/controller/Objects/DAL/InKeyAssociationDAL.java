/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.DAL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        StringBuilder params = new StringBuilder();
        for (InImportKey impTable : listImp) {
            params.append(impTable.getImportKeyID() + ",");
        }

        //REMODELAR ESTA QUERY PARA SQLITE (NAO EXISTE A TABELA DUAL)
        StringBuilder queryString = new StringBuilder("With listKeysAssoc as (");
        queryString.append(" select regexp_substr(?listKeys,'[^,]+', 1, level) IMPORTKEYID");
        queryString.append(" from dual connect by regexp_substr(?listKeys, '[^,]+', 1, level) is not null");
        queryString.append(")");
        queryString.append(" Select IN_KEYASSOCIATION.* from DPM_ED.IN_KEYASSOCIATION ");
        queryString.append(" inner join listKeysAssoc ");
        queryString.append(" on IN_KEYASSOCIATION.IMPORTKEYID = listKeysAssoc.IMPORTKEYID ");

        try {
            queryResult = jpa.getTypedNativeResultList(queryString.toString(), "listKeys", params.toString());
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
