package com.example.demo.controller.Objects.Entities.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.DTOs.DatapointItensDTO;
import com.example.demo.Data.Connection;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ItemCategory;
import com.example.demo.controller.Objects.Import.InImportedTablesTemp;

import jakarta.persistence.*;


public class ItemCategoryDAL {
 
    
    /** Método para obter todos os códigos (Utilizados em determinadas Celulas e no código de desagregacao), EX: portugal - PT**/
    public static List<ItemCategory> getListItemCategory(){
        EntityManager entityManager = Connection.getEm();
        List<ItemCategory> listOfModules = new ArrayList<>();
        
        try {
            listOfModules = entityManager.createNativeQuery("Select a.ITEMID,a.STARTRELEASEID,a.CODE,a.SIGNATURE  from ItemCategory a inner join Item b on a.itemid = b.itemid where b.isactive = 1", ItemCategory.class)
                                                            .getResultList();
            
            
        } catch (NoResultException nrex){
            nrex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            entityManager.close();
        }
        return listOfModules;
    }
    
    
    public static Set<Integer> getListItemCategoryBasedOnTableVariableVID(Integer VariableVID){
        EntityManager entityManager = Connection.getEm();
        List<Integer> listOfModules = new ArrayList<>();
        try {
            listOfModules = entityManager.createNativeQuery("SELECT IT.ITEMID \n" +
                                                            "FROM VARIABLEVERSION VV\n" +
                                                            "JOIN PROPERTYCATEGORY PC ON VV.PROPERTYID = PC.PROPERTYID \n" +
                                                            "JOIN CATEGORY CG ON PC.CATEGORYID = CG.CATEGORYID \n" +
                                                            "JOIN ITEMCATEGORY IT ON CG.CATEGORYID = IT.CATEGORYID \n" +
                                                            "WHERE VV.VARIABLEVID = ?")
                                                            .setParameter(1, VariableVID)
                                                            .getResultList();
            
        } catch (NoResultException nrex){
            nrex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            entityManager.close();
        }
        return new HashSet<>(listOfModules);
    }
    
    public static Set<Integer> getSetIDsItemCategoryOfTableDesagregationCode(InImportedTablesTemp importedTable, String direction){
        return getSetIDsItemCategoryOfTableDesagregationCode(importedTable.getTableVersion().getTableVID(),importedTable.getTableVersion().getTable().getTableId(),direction);
    }
    
    public static Set<Integer> getSetIDsItemCategoryOfTableDesagregationCode(int TableVID, int TableID,String direction){
        EntityManager entityManager = Connection.getEm();
        List<?> listOfModules = new ArrayList<>();
        try {
            listOfModules = entityManager.createNativeQuery("SELECT IT.ITEMID \n" +
                                                            " FROM TABLEVERSION TV \n" +
                                                            " JOIN KEYCOMPOSITION KC ON KC.KEYID = TV.KEYID \n" +
                                                            " JOIN VARIABLEVERSION VV ON KC.VARIABLEVID = VV.VARIABLEVID \n" +
                                                            " JOIN HEADERVERSION HV ON HV.KEYVARIABLEVID = VV.VARIABLEVID \n" +
                                                            " JOIN HEADER H ON H.HEADERID = HV.HEADERID \n" +
                                                            " JOIN PROPERTY PR ON PR.PROPERTYID = VV.PROPERTYID \n" +
                                                            " JOIN PROPERTYCATEGORY PC ON PR.PROPERTYID = PC.PROPERTYID \n" +
                                                            " JOIN CATEGORY CG ON PC.CATEGORYID = CG.CATEGORYID \n" +
                                                            " JOIN ITEMCATEGORY IT ON CG.CATEGORYID = IT.CATEGORYID \n" +
                                                            " WHERE TV.TABLEVID = ? AND H.TABLEID = ? AND H.DIRECTION = ?")
                                                            .setParameter(1, TableVID)
                                                            .setParameter(2, TableID)
                                                            .setParameter(3, direction)
                                                            .getResultList();
            
        } catch (NoResultException nrex){
            nrex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            entityManager.close();
        }
        return listOfModules.stream().map(result -> ((Number) result).intValue()).collect(Collectors.toSet());
    }
    
    public static List<DatapointItensDTO> getListOFPossibleItensOfDatapoit(int tableVID, String direction, String reportCoordinates, LocalDate referenceDate){
        JPA<DatapointItensDTO> jpa = new JPA<DatapointItensDTO>(DatapointItensDTO.class);
        List<DatapointItensDTO> listOfFiles = new ArrayList<>();
        
        try {
            listOfFiles = jpa.getMappedFileQueryResultList("SQL_Queries/DatapointPossibleValues.sql", "DatapointItensDTO",
                        "tablevid", tableVID, 
                        "direction",direction ,
                        "reportCoordinates", Optional.ofNullable(reportCoordinates).orElse(""),
                        "referenceDate", referenceDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS).toString());

            
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return listOfFiles;
    }
}
