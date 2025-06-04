/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

import com.example.demo.DTOs.*;
import com.example.demo.Data.Access.*;
import com.example.demo.controller.Objects.Conf.*;
import com.example.demo.controller.Objects.Constants;
import com.example.demo.controller.Objects.Utils;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.controller.Objects.Import.*;

public class InImportedTablesDAL {

    public static List<InImportedTablesTemp> getListOfImportedMaps(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain, IO io) {
        JPA<InImportedTablesTemp> jpa = new JPA<>(InImportedTablesTemp.class);
        domain = domain != null ? (domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain) : null;
        String queryStr = Utils.getResource("GetImportedTablesForGeneration.sql");
        
        List<InImportedTablesTemp> listOfMaps = new ArrayList<>();
        try {
            listOfMaps = jpa.getTypedNativeResultList(queryStr,
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
        String queryStr = Utils.getResource("GetImportedTables.sql");
        
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
	
    
    public static List<CommonDatapointValidationDTO> getPossibleDataPointsConflicts(TableVersionDPM table, LocalDate referenceDate, String domain, ConfEntities entity, IO io){
        final Logger LOG = Logger.getLogger(InImportedTablesDAL.class.getName());

        JPA<CommonDatapointValidationDTO> jpa = new JPA<CommonDatapointValidationDTO>(CommonDatapointValidationDTO.class);
        List<CommonDatapointValidationDTO> possibleDatapointsConflictsList = new ArrayList<>();
        domain = domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain;
        String queryStr = Utils.getResource("GetCommonDatapoints20.sql");
        try {
            possibleDatapointsConflictsList = jpa.getNativeResultListWithMapping(queryStr, "CommonDatapointValidation", 
                    "ioId", io.getIoId(),
                    "tableVID", table.getTableVID(),
                    "tableID",table.getTable().getTableId());
            
        } catch (Exception e) {
            LOG.error("Erro na query GetCommonDatapoints20.sql:" + e.getMessage());
            e.printStackTrace();
        }finally{
            jpa.close();
        }
        return possibleDatapointsConflictsList;
    }

}