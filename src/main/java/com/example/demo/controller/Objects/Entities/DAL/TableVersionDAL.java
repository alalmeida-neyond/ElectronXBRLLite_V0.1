
package com.example.demo.controller.Objects.Entities.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;

import org.jboss.logging.Logger;

import com.example.demo.Data.Connection;
import com.example.demo.Data.Access.*;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.Entities.DPMOrigin.TableVersionDPM;


public class TableVersionDAL {
    
    private static final Logger LOG = Logger.getLogger(TableVersionDAL.class.getName());

    public static List<TableVersionDPM> getAllFilesImported(ModuleVersion module, LocalDate referenceDate) {
        if (module == null || referenceDate == null) {
            return null;
        }
        JPA<TableVersionDPM> jpa = new JPA<TableVersionDPM>(TableVersionDPM.class);
        List<TableVersionDPM> listOfFiles = new ArrayList<>();
        try {
            StringBuilder queryString = new StringBuilder("Select c.* from MODULEVERSIONCOMPOSITION b ");
            queryString.append(" inner join MODULEVERSION a on b.ModuleVID = a.ModuleVID ");
            queryString.append(" inner join tableversion c on c.TableVid = b.TableVid and c.Tableid = b.Tableid ");
            queryString.append(" where a.ModuleVID = :moduleVID and (datetime(a.fromreferencedate / 1000, 'unixepoch') <= :referencedate ");
            queryString.append(" and (a.toreferencedate is null or datetime(a.toreferencedate / 1000, 'unixepoch') >= :referencedate)) ");

            listOfFiles = jpa.getTypedNativeResultList(queryString.toString(),
                    "moduleVID", module.getModuleVID(),
                    "referencedate", referenceDate.toString());

        } catch (Exception e) {
            LOG.error("Erro na query TableVersionDAL:" + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return listOfFiles;
    }

    public static Map<String, Integer> getPropertiesKeyTypes(Integer tableVId, LocalDate referenceDate){
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        Map<String, Integer> properties = new HashMap<>();
        
        try {
            List<Object[]> results = jpa.getFileQueryResultList("SQL_Queries/PropertiesIdentifier.sql",
                    "tableVId", String.valueOf(tableVId),
                    "referenceDate", referenceDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS).toString(),
                    "format",Constants.ISOBASEFORMAT8601SQLite);
            
            if(results != null && !results.isEmpty()){
                for(Object[] result : results){
                    properties.put(result[0].toString(), Integer.valueOf(result[1].toString()));
                }
            }
        } catch (Exception e) {
            LOG.error("Erro na query getPropertiesKeyTypes", e);
        } finally {
            Connection.close(jpa.getEm().em);
        }

        return properties;

    }

    public static Set<Integer> getAllTableMonetaryVariablesWithParamUnit(int tableVID){
        JPA<Integer> jpa = new JPA<Integer>(Integer.class);
        List<Object[]> resultList = new ArrayList<>();

        String query = Utils.getResource("SQL_Queries/GetAllTableMonetaryVariablesWithParamUnit.sql");
        try {
            resultList = jpa.getNativeResultList(query,
                    "tableVID", String.valueOf(tableVID),
                    "datatypeID",String.valueOf(Constants.DATATYPEMONETARY), 
                    "itemID", String.valueOf(Constants.MONETARYVARIABLEWITHUNITITEMID));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
        
        Set<Integer> resultSet = new HashSet<>();
        for(Object resultRow : resultList){
            Integer variableVID = ((BigDecimal)resultRow).intValueExact();
            resultSet.add(variableVID);
        }
        
        return resultSet;
    }

}