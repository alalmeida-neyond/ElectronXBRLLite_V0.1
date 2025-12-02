
package com.example.demo.controller.Objects.Entities.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.demo.Data.Connection;
import com.example.demo.Data.Access.*;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.Entities.DPMOrigin.TableDPM;
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
            StringBuilder queryString = new StringBuilder("Select c.* from DPM_MD.MODULEVERSIONCOMPOSITION b ");
            queryString.append(" inner join DPM_MD.MODULEVERSION a on b.ModuleVID = a.ModuleVID ");
            queryString.append(" inner join DPM_MD.tableversion c on c.TableVid = b.TableVid and c.Tableid = b.Tableid ");
            queryString.append(" where a.ModuleVID = ?moduleVID and (a.fromreferencedate <= to_date(?referencedate,'YYYYMMDD') ");
            queryString.append(" and (a.toreferencedate is null or a.toreferencedate >= to_date(?referencedate,'YYYYMMDD'))) ");

            listOfFiles = jpa.getTypedNativeResultList(queryString.toString(),
                    "moduleVID", module.getModuleVID(),
                    "referencedate", referenceDate.format(Constants.dateFormat));

        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro na query TableVersionDAL:" + e.getMessage(), e);
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
                    "referenceDate", referenceDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS),
                    "format",Constants.ISOBASEFORMAT8601);
            
            if(results != null && !results.isEmpty()){
                for(Object[] result : results){
                    properties.put(result[0].toString(), Integer.valueOf(result[1].toString()));
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro na query getPropertiesKeyTypes", e);
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
            Integer variableVID = (Integer)resultRow;
            resultSet.add(variableVID);
        }
        
        return resultSet;
    }

    public static Map<Integer, Integer> getDesagregationCodeTypeOfMaps(ModuleVersion module){
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> resultList = new ArrayList<>();
        Map<Integer, Integer> mapsWithDesagCodeTypes = new HashMap<>();
        
        try {
            resultList = jpa.getFileQueryResultList("SQL_Queries/GetDesagregationCodeTypeOfMaps.sql",
                    "moduleVID", module.getModuleVID(),
                    "desagregationCodeTypeFix", Constants.DESAGREGATIONCODEFIXEDTYPE,
                    "directionZ", Constants.SHEETCOORDINATE,
                    "falseNumber", Constants.FALSEASNUMBER,
                    "desagregationCodeTypeNormal", Constants.DESAGREGATIONCODETYPE,
                    "trueNumber", Constants.TRUEASNUMBER
            );

            if (resultList != null && !resultList.isEmpty()) {
                for (Object[] resultRow : resultList) {
                    mapsWithDesagCodeTypes.put(Integer.valueOf(resultRow[0].toString()), Integer.valueOf(resultRow[1].toString()));
                }
            }
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro na query GetDesagregationCodeTypeOfMaps", e);
        } finally {
            jpa.close();
        }
        
        return mapsWithDesagCodeTypes;
    }

    public static TableDPM getTableDPM(Integer tableVid){
        JPA<TableDPM> jpa = new JPA<TableDPM>(TableDPM.class);
        List<TableDPM> results = new ArrayList();
        try {
            results = jpa.getTypedNativeResultList("select t.* from DPM_MD.tableversion tv inner join \"TABLE\" t on t.tableid = tv.tableid where tv.tablevid = ?tablevid",
                    "tablevid",tableVid);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro na query GetDesagregationCodeTypeOfMaps", e);
        } finally {
            jpa.close();
        }
        return (results.size()>0)? results.get(0): null;
    }
}