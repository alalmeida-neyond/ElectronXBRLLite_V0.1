/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Entities.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

import com.example.demo.DTOs.*;
import com.example.demo.Data.Access.*;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Entities.Conf.*;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.Entities.DPMOrigin.TableVersionDPM;
import com.example.demo.controller.Objects.IO.IO;
import com.example.demo.controller.Objects.Import.*;

public class InImportedTablesDAL {

    public static List<InImportedTablesTemp> getListOfImportedMaps(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain, IO io) {
        JPA<InImportedTablesTemp> jpa = new JPA<>(InImportedTablesTemp.class);
        domain = domain != null ? (domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain) : null;
        String queryStr = Utils.getResource("SQL_Queries/GetImportedTablesForGeneration.sql");
        
        List<InImportedTablesTemp> listOfMaps = new ArrayList<>();
        try {
            listOfMaps = jpa.getTypedNativeResultList(queryStr,
                                "processOkEmpty",String.valueOf(Constants.processoOkEmpty),
                    "processOkDeleted", String.valueOf(Constants.processoOkDeleted),
                    "desagregationCodeType", String.valueOf(Constants.DESAGREGATIONCODETYPE),
                    "ioId", io.getIoId());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfMaps;
    }
    
    public static List<InImportedTablesTemp> getListOfImportedMapsToValidate(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain, IO io) {
        JPA<InImportedTablesTemp> jpa = new JPA<>(InImportedTablesTemp.class);
        domain = domain != null ? (domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain) : null;
        String queryStr = Utils.getResource("SQL_Queries/GetImportedTables.sql");
        
        List<InImportedTablesTemp> listOfMaps = new ArrayList<>();
        try {
            listOfMaps = jpa.getTypedNativeResultList(queryStr,
                    "ioId", io.getIoId(),
                    "stateOk", String.valueOf(Constants.processoOk),
                    "processOkDeleted", String.valueOf(Constants.processoOkDeleted));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfMaps;
    }

    public static List<TableVersionDPM> getMapsToValidate(ModuleVersion module) {
        JPA<TableVersionDPM> jpa = new JPA<>(TableVersionDPM.class);
        String queryStr = Utils.getResource("SQL_Queries/GetTablesToValidate.sql");

        List<TableVersionDPM> listOfMaps = new ArrayList<>();
        try {
            listOfMaps = jpa.getMappedFileQueryResultList("SQL_Queries/GetTablesToValidate.sql", "TableVersionDPMMapping",
            "modulevid", String.valueOf(module.getModuleVID()));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfMaps;
    }
	
    
    public static List<CommonDatapointValidationDTO> getPossibleDataPointsConflicts(TableVersionDPM table, LocalDate referenceDate, String domain, ConfEntities entity, IO io){
        final Logger LOG = Logger.getLogger(InImportedTablesDAL.class.getName());

        JPA<CommonDatapointValidationDTO> jpa = new JPA<CommonDatapointValidationDTO>(CommonDatapointValidationDTO.class);
        List<CommonDatapointValidationDTO> possibleDatapointsConflictsList = new ArrayList<>();
        domain = domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain;
        String queryStr = Utils.getResource("SQL_Queries/GetCommonDatapoints20.sql");
        try {
            possibleDatapointsConflictsList = jpa.getNativeResultListWithMapping(queryStr, "CommonDatapointValidation", 
                    "tableVID", table.getTableVID(),
                    "tableID",table.getTable().getTableId(),
                    "typeStateOk", Constants.tipoStateOK,
                    "actionId",   Constants.actionImport,
                    "referenceDate", io.getReferenceDate().toString(), 
                    "domain", io.getDomain(), 
                    "entityId", io.getEntity().getEntityID()
                );
            
        } catch (Exception e) {
            LOG.error("Erro na query GetCommonDatapoints20.sql:" + e.getMessage());
            e.printStackTrace();
        }finally{
            jpa.close();
        }
        return possibleDatapointsConflictsList;
    }

    public static List<DataTypeHasUnitDTO> getListOfImportedDatatypes(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain) {
        JPA<DataTypeHasUnitDTO> jpa = new JPA<>(DataTypeHasUnitDTO.class);
        domain = domain != null ? (domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain) : null;

        List<DataTypeHasUnitDTO> listOfDataTypes = new ArrayList<>();
        try {
            listOfDataTypes = jpa.getMappedFileQueryResultList("SQL_Queries/GetImportedDatatypesHasUnit.sql", "dataTypeHasUnitDTOMapping",
                    "actionId", String.valueOf(Constants.actionImport),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "referenceDate", referenceDate != null ? referenceDate.format(Constants.dateFormat) : null,
                    "format", Constants.ISOBASEFORMATSQlite,
                    "domain", domain != null ? domain.toUpperCase() : null,
                    "moduleVID", module != null ? String.valueOf(module.getModuleVID()) : null,
                    "entityId", entity != null ? String.valueOf(entity.getEntityID()) : null,
                    "monetaryDatatypeId", String.valueOf(Constants.DATATYPEMONETARY),
                    "itemId", String.valueOf(Constants.MONETARYVARIABLEWITHUNITITEMID)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfDataTypes;
    }

}