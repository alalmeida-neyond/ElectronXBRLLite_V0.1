package com.example.demo.controller.Objects.Managers;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.DTOs.HeaderDTO;
import com.example.demo.Data.Connection;

import jakarta.persistence.*;

public class HeaderManager {
        
    
    public static List<HeaderDTO> getListOfHeaderForATableVid(int tableVid){
        EntityManager entityManager = Connection.getEm();
        List<HeaderDTO> listOfModules = new ArrayList<>();
        List<Object[]> tempList = new ArrayList<>();
        try {
            tempList = entityManager.createNativeQuery("SELECT H.HEADERID, HV.CODE, H.DIRECTION \n" +
                                                            "FROM TABLEVERSIONHEADER TVH \n" +
                                                            "Inner JOIN HEADERVERSION HV ON TVH.HEADERVID = HV.HEADERVID \n" +
                                                            "Inner JOIN HEADER H ON H.HEADERID = HV.HEADERID \n" +
                                                            "WHERE TVH.TABLEVID = ?")
                                                            .setParameter(1, tableVid)
                                                            .getResultList();
            
            for(Object[] obj : tempList){
                listOfModules.add(new HeaderDTO(Integer.valueOf(obj[0].toString()),obj[1].toString(), obj[2].toString().toCharArray()[0]));
            }   
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            entityManager.close();
        }
        return listOfModules;
    }
    
}