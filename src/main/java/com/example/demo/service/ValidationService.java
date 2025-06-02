package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.DTOs.ValidationResultsDetailsDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.*;
import com.example.demo.controller.Objects.Entities.IO;
public class ValidationService {

    public List<ValidationResultsDetailsDTO> getValidationResults(Integer ioId) {
        JPA<ValidationResultsDetailsDTO> jpa = new JPA<>(ValidationResultsDetailsDTO.class);
        List<ValidationResultsDetailsDTO> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList("GetValidationsResultsDetails.sql",
                    "ValidationResultsDetailsRow","ioId", ioId);

            if(results == null)
            {
                return new ArrayList<>();
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Object[]> getIOResults() {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> result = new ArrayList<Object[]>();

        try {
            StringBuilder queryString = new StringBuilder("Select io.ioid as ioid, io.modulevid as module, io.entityid as entity");
            queryString.append(", io.domain as domain, date(io.referencedate) as referenceDate, io.actionid");
            queryString.append(" from IO io");
            queryString.append(" where io.ACTIONID = 2;");
            result = jpa.getTypedNativeResultList(queryString.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
    }
}
