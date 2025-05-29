/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.DTOs.*;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.*;
import com.example.demo.controller.Objects.Entities.*;

public class OutValidationResultDAL {

    public static List<ValidationResultsDTO> getValidationResults(IO ioVal, ModuleVersion moduleVersion, LocalDate referenceDate, ConfEntities entity, String domain) {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> listOfResultstemp = new ArrayList<>();
        List<ValidationResultsDTO> listOfResults = new ArrayList<>();

        domain = domain != null ? domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase() : null;
        String moduleVID = moduleVersion != null && String.valueOf(moduleVersion.getModuleVID()) != null ? String.valueOf(moduleVersion.getModuleVID()) : null;
        String entityID = entity != null ? String.valueOf(entity.getEntityID()) : null;
        try {
            listOfResultstemp = jpa.getNativeResultList(Utils.getResource("GetValidationsResults.sql"),
                    "referenceDate", referenceDate != null ? referenceDate.format(Constants.dateFormat) : null,
                    "format",Constants.ISOBASEFORMAT,
                    "entityID", entityID,
                    "domain", domain,
                    "moduleVID", moduleVID,
                    "actionValidateId", Constants.actionValidation,
                    "typeStateOk", Constants.tipoStateOK,
                    "stateOk", Constants.processoOk,
                    "ioid", ioVal == null ? null : ioVal.getIoId());

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        if (listOfResultstemp != null && !listOfResultstemp.isEmpty()) {
            for (Object obj[] : listOfResultstemp) {
                ValidationResultsDTO validationResults = new ValidationResultsDTO();
                validationResults.setValidationResultID(obj[0] == null ? null : obj[0].toString());
                validationResults.setRefDate(obj[1] == null ? null : obj[1].toString());
                validationResults.setModulo(obj[2] == null ? null : obj[2].toString());
                validationResults.setEntity(obj[3] == null ? null : obj[3].toString());
                validationResults.setDomain(obj[4] == null ? null : obj[4].toString());
                validationResults.setSeverity(obj[5] == null ? null : obj[5].toString());
                validationResults.setRegra(obj[6] == null ? null : obj[6].toString());
                validationResults.setOrigem(obj[7] == null ? null : obj[7].toString());
                validationResults.setResultado(obj[8] == null ? null : obj[8].toString());
                validationResults.setDataProcessamento(obj[9] == null ? null : obj[9].toString());
                validationResults.setUser(obj[10] == null ? null : obj[10].toString());
                validationResults.setIoId(obj[11] == null ? null : Integer.valueOf(obj[11].toString()));
                listOfResults.add(validationResults);
            }
        }

        return listOfResults;
    }         
    
    public static List<ValidationResultsDetailsDTO> getValidationResultsDetails(String validationResultID, ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain) {
        JPA<ValidationResultsDetailsDTO> jpa = new JPA<ValidationResultsDetailsDTO>(ValidationResultsDetailsDTO.class);
        List<ValidationResultsDetailsDTO> listOfResults = new ArrayList<>();

        try {
            listOfResults = jpa. getNativeResultListWithMapping(Utils.getResource("GetValidationsResultsDetails.sql"), "ValidationResultsDetailsRow",
                "stateOk", Constants.processoOk,
                "validationResultId", validationResultID,
                "typeStateOk", Constants.tipoStateOK,                   
                "actionValidateId", Constants.actionValidation,         
                "referenceDate", referenceDate,
                "domain", domain,
                "moduleVID", module != null ? String.valueOf(module.getModuleVID()) : null,
                "entityID", entity != null ? String.valueOf(entity.getEntityID()) : null);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfResults;
    }
    
    public static List<Object[]> getValidationResultsDetailsForGeneration(IO ioIdValidate) {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> listOfResultstemp = new ArrayList<>();

        try {
            listOfResultstemp = jpa.getNativeResultList(Utils.getResource("GetValidationsResultsDetailsForGenDownload.sql"),
                "stateOk", Constants.processoOk,
                "ioId", String.valueOf(ioIdValidate.getIoId()),
                "typeStateOk", Constants.tipoStateOK,                   
                "actionValidateId", Constants.actionValidation);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }

        return listOfResultstemp;
    }
    
    public static OutValidationResult getValidationResultById(String id) {
        JPA<OutValidationResult> jpa = new JPA<OutValidationResult>(OutValidationResult.class);
        OutValidationResult validationResult = null;
        try {
            validationResult = jpa.getSimpleResult("validationResultId", id);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
             jpa.close();
        }
        return validationResult;
    }
}
