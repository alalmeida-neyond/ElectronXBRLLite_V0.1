package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.example.demo.DTOs.ValidationResultsDetailsDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
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

    public List<LogImportProcess> getImportResults(Integer ioId) {
        JPA<LogImportProcess> jpa = new JPA<>(LogImportProcess.class);
        List<LogImportProcess> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList("SQL_Queries/GetImportedDetails.sql",
                    "ValidationResultsDetailsRow",
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

    public List<ValidationResultsDetailsDTO> getGenerationResults(Integer ioId) {
        JPA<ValidationResultsDetailsDTO> jpa = new JPA<>(ValidationResultsDetailsDTO.class);
        List<ValidationResultsDetailsDTO> results = new ArrayList<>();
        try {
            String moduleVersionFromIO = getModuleVersionFromIO(ioId);

            LOG.info("Module Version:" + moduleVersionFromIO);
            
            results = jpa.getMappedFileQueryResultList("SQL_Queries/GetGenerationDetails.sql",
                    "ValidationResultsDetailsRow",
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
        List<Object[]> resultsIO = new ArrayList<Object[]>();

        try {
            resultsIO = jpa.getNativeResultList(Utils.getResource("SQL_Queries/GetInformationIO.sql"),
                    "importActionID", Constants.actionImport,
                    "validationActionID", Constants.actionValidation,
                    "generationActionID", Constants.actionGeneration);

            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
        if(resultsIO == null)
        {
            return new ArrayList<>();
        }
        return resultsIO;
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
