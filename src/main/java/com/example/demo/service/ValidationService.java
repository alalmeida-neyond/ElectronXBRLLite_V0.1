package com.example.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.example.demo.DTOs.GenerateXBRLDetailsDTO;
import com.example.demo.DTOs.ImportedDetailsDTO;
import com.example.demo.DTOs.ValidationResultsDetailsDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Beans.DefaultBean;
import com.example.demo.controller.Objects.Entities.Conf.ConfEntities;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
public class ValidationService extends DefaultBean<ValidationResultsDetailsDTO>{
    private final static Logger LOG = Logger.getLogger(ValidationService.class.getName());

    private static final String PERSISTENCE_UNIT_NAME_OD = "dpm2md";
    EntityManagerFactory emf_od = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME_OD);
    
    public EntityManager getEntityManager() {
        return emf_od.createEntityManager();
    }
    
    public void close(){
        if(emf_od.isOpen()){
            emf_od.close();
        }
    }

    public List<ValidationResultsDetailsDTO> getValidationResults(Integer ioId) {
        JPA<ValidationResultsDetailsDTO> jpa = new JPA<>(ValidationResultsDetailsDTO.class);
        List<ValidationResultsDetailsDTO> results = new ArrayList<>();
        try {
            String moduleVersionFromIO = getModuleVersionFromIO(ioId);

            LOG.info("Module Version:" + moduleVersionFromIO);
            
            results = jpa. getNativeResultListWithMapping(Utils.getResource("SQL_Queries/GetValidationsResultsDetails.sql"), "ValidationResultsDetailsRow",
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
                    "format",Constants.ISOBASEFORMAT8601,
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
        List<Object[]> resultsIO = new ArrayList<Object[]>();

        try {
            resultsIO = jpa.getNativeResultList(Utils.getResource("SQL_Queries/GetInformationIO.sql"),
                    "ignoreActionID", Constants.actionIgnore,
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
        JPA<ModuleVersion> jpa = new JPA<ModuleVersion>(ModuleVersion.class);
        List<ModuleVersion> result = new ArrayList<ModuleVersion>();
        List<String> resulStrings = new ArrayList<>();

        try {
            StringBuilder queryString = new StringBuilder("Select mv.modulevid, mv.code");
            queryString.append(" from ModuleVersion mv");
            result = jpa.getTypedNativeResultList(queryString.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        for (ModuleVersion moduleVersion : result) {
            resulStrings.add(moduleVersion.getCode());
        }

        return resulStrings;
    }

    public String getModuleVersionFromIO(Integer ioID) {
        JPA<ModuleVersion> jpa = new JPA<ModuleVersion>(ModuleVersion.class);
        List<ModuleVersion> result = new ArrayList<ModuleVersion>();

        try {
            StringBuilder queryString = new StringBuilder("Select io.modulevid as moduleVID");
            queryString.append(" from DPM_OD.IO io where ioid = ?ioid ");
            result = jpa.getTypedNativeResultList(queryString.toString(),
                    "ioid", ioID);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return Integer.toString(result.get(0).getModuleVID());
    }
}
