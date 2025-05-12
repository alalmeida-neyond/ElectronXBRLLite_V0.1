/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.DTOs.*;
import com.example.demo.Data.Access.*;
import com.example.demo.controller.Objects.*;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.controller.Objects.Generation.OutXBRLGenerated;
import com.example.demo.controller.Objects.Import.*;

import jakarta.persistence.NoResultException;


public class InImportedValuesDAL {

    public static List<ImportedValuesDTO> getListOfImportedValues(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain) {
        JPA<ImportedValuesDTO> jpa = new JPA<>(ImportedValuesDTO.class);
        domain = domain != null ? (domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain) : null;
        List<ImportedValuesDTO> listOfValues = new ArrayList<>();
        try {
            listOfValues = jpa.getNativeResultListWithMapping(Utils.getResource("GetImportedValues.sql"), "ImportedValuesRow",
                    "actionId", String.valueOf(Constants.actionImport),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "desagregationCodeType", String.valueOf(Constants.DESAGREGATIONCODETYPE),
                    "desagregationCodeFixedType", String.valueOf(Constants.DESAGREGATIONCODETYPE),
                    "referenceDate", referenceDate != null ? referenceDate.format(Constants.dateFormat) : null,
                    "format",Constants.ISOBASEFORMAT,
                    "domain", domain != null ? domain.toUpperCase() : null,
                    "moduleVID", module != null ? String.valueOf(module.getModuleVID()) : null,
                    "entityId", entity != null ? String.valueOf(entity.getEntityID()) : null);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfValues;
    }

    public static List<InImportedValuesTemp> getListOfImportedCellBasedOnImportedTable(InImportedTablesTemp impTab) {
        JPA<InImportedValuesTemp> jpa = new JPA<>(InImportedValuesTemp.class);
        List<InImportedValuesTemp> resultList = new ArrayList<>();
        try {
            resultList = jpa.getTypedNativeResultList("Select * from DPM_ED.in_importedvaluestemp where importedTableId = ?importedTableId",
                    "importedTableId", impTab.getImportedTableId());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return resultList;
    }
   
    
    //public static List<Object[]> getDiferences(OutXBRLGenerated row, String user){
    public static List<Object[]> getDiferences(OutXBRLGenerated row){
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> resultList = new ArrayList<>();
        
        try {
            /*resultList = jpa.getNativeResultList(Utils.getResource("GetDifferences.sql"),
                        "typeStateOk", String.valueOf(Constants.tipoStateOK),
                        "actionImportId", String.valueOf(Constants.actionImport),
                        "referenceDate", row.getReferenceDate().format(Constants.dateFormat),
                        "domain", row.getDomain(),
                        "format", Constants.ISOBASEFORMAT,
                        "entityId", row.getEntity().getEntityID(),
                        "moduleVID", row.getModuleVersion().getModuleVID(),
                        "ioIdGenerate", row.getIo().getIoId(),
                        "desagregationCodeType", Constants.DESAGREGATIONCODETYPE,
                        "rowKeyType", Constants.ROWKEYTYPE, 
                        "currentDate", LocalDate.now().format(Constants.DATEFORMATISO8601),
                        "currentUser", user);*/

            resultList = jpa.getNativeResultList(Utils.getResource("GetDifferences.sql"),
                        "typeStateOk", String.valueOf(Constants.tipoStateOK),
                        "actionImportId", String.valueOf(Constants.actionImport),
                        "referenceDate", row.getReferenceDate().format(Constants.dateFormat),
                        "domain", row.getDomain(),
                        "format", Constants.ISOBASEFORMAT,
                        "entityId", row.getEntity().getEntityID(),
                        "moduleVID", row.getModuleVersion().getModuleVID(),
                        "ioIdGenerate", row.getIo().getIoId(),
                        "desagregationCodeType", Constants.DESAGREGATIONCODETYPE,
                        "rowKeyType", Constants.ROWKEYTYPE, 
                        "currentDate", LocalDate.now().format(Constants.DATEFORMATISO8601)); 
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return resultList;
    }
}
