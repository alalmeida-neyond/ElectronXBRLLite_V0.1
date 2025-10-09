package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.example.demo.DTOs.GenerateXBRLDetailsDTO;
import com.example.demo.DTOs.ImportedDetailsDTO;
import com.example.demo.DTOs.ValidationResultsDetailsDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Beans.DefaultBean;
import com.example.demo.controller.Objects.Entities.Logs.LogImportProcess;
public class ValidationService extends DefaultBean<ValidationResultsDetailsDTO>{
    private final static Logger LOG = Logger.getLogger(ValidationService.class.getName());

    public List<ValidationResultsDetailsDTO> getValidationResults(Integer ioId) {
        JPA<ValidationResultsDetailsDTO> jpa = new JPA<>(ValidationResultsDetailsDTO.class);
        List<ValidationResultsDetailsDTO> results = new ArrayList<>();
        try {
            String moduleVersionFromIO = getModuleVersionFromIO(ioId);

            LOG.info("Module Version:" + moduleVersionFromIO);
            
            results = jpa.getMappedFileQueryResultList("SQL_Queries/GetValidationsResultsDetails.sql",
                    "ValidationResultsDetailsRow",
                    "ioid", ioId,
                    "moduleVID", getModuleVersion() != null ? String.valueOf(getModuleVersion().getModuleVID()) : "");

            
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

    public List<ImportedDetailsDTO> getImportResults(Integer ioId) {
        JPA<ImportedDetailsDTO> jpa = new JPA<>(ImportedDetailsDTO.class);
        List<ImportedDetailsDTO> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList("SQL_Queries/GetImportedDetails.sql",
                    "ImportedDetailsDTOMapping",
                    "format",Constants.ISOBASEFORMAT8601SQLite,
                    "dateTimeFormat",Constants.DATETIMEFORMATSQLite,
                    "ioid", ioId);

            
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

    public List<GenerateXBRLDetailsDTO> getGenerationResults(Integer ioId) {
        JPA<GenerateXBRLDetailsDTO> jpa = new JPA<>(GenerateXBRLDetailsDTO.class);
        List<GenerateXBRLDetailsDTO> results = new ArrayList<>();
        try {
            String moduleVersionFromIO = getModuleVersionFromIO(ioId);

            LOG.info("Module Version:" + moduleVersionFromIO);
            
            results = jpa.getMappedFileQueryResultList("SQL_Queries/GetGenerationDetails.sql",
                    "GenerateXBRLDetailsDTOMapping",
                    "format",Constants.ISOBASEFORMAT8601SQLite,
                    "dateTimeFormat",Constants.DATETIMEFORMATSQLite,
                    "ioid", ioId);

            
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
            StringBuilder queryString = new StringBuilder("Select io.ioid as ioid, io.io_stateid as stateid,mv.code as module");
            queryString.append(", ce.leicode as entity, io.domain as domain, date(io.referencedate) as referenceDate, io.actionid");
            queryString.append(" from IO io inner join ModuleVersion mv on mv.modulevid = io.modulevid inner join CONF_ENTITIES ce on ce.entityid = io.entityid");
            queryString.append(" where io.ACTIONID = 2;");
            result = jpa.getTypedNativeResultList(queryString.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
    }

    public List<String> getModules() {
        JPA<String> jpa = new JPA<String>(String.class);
        List<String> result = new ArrayList<String>();

        try {
            StringBuilder queryString = new StringBuilder("Select mv.code as module");
            queryString.append(" from ModuleVersion mv;");
            result = jpa.getTypedNativeResultList(queryString.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
    }

    public String getModuleVersionFromIO(Integer ioID) {
        JPA<String> jpa = new JPA<String>(String.class);
        List<String> result = new ArrayList<String>();

        try {
            StringBuilder queryString = new StringBuilder("Select io.modulevid as moduleVID");
            queryString.append(" from IO io where ioid = :ioid ");
            result = jpa.getTypedNativeResultList(queryString.toString(),
                    "ioid", ioID);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result.get(0);
    }
}
