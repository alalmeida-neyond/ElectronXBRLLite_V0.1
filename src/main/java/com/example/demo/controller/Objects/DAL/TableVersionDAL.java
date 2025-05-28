
package com.example.demo.controller.Objects.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

import com.example.demo.Data.Connection;
import com.example.demo.Data.Access.*;
import com.example.demo.controller.Objects.Constants;
import com.example.demo.controller.Objects.Entities.*;


public class TableVersionDAL {
    
    private static final Logger LOG = Logger.getLogger(TableVersionDAL.class.getName());

    public static List<TableVersionDPM> getAllFilesImported(ModuleVersion module, LocalDate referenceDate) {
        if (module == null || referenceDate == null) {
            return null;
        }
        JPA<TableVersionDPM> jpa = new JPA<TableVersionDPM>(TableVersionDPM.class);
        List<TableVersionDPM> listOfFiles = new ArrayList<>();
        try {
            /*StringBuilder queryString = new StringBuilder("Select c.* from MODULEVERSIONCOMPOSITION b ");
            queryString.append(" inner join MODULEVERSION a on b.ModuleVID = a.ModuleVID ");
            queryString.append(" inner join tableversion c on c.TableVid = b.TableVid and c.Tableid = b.Tableid ");
            queryString.append(" where a.ModuleVID = :moduleVID and (a.fromreferencedate <= :referencedate ");
            queryString.append(" and (a.toreferencedate is null or a.toreferencedate >= :referencedate)) ");*/

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

    public static List<TableVersionDPM> getFilesImportedForIO(IO io) {
        if (io == null) {
            return null;
        }
        JPA<TableVersionDPM> jpa = new JPA<TableVersionDPM>(TableVersionDPM.class);
        List<TableVersionDPM> listOfFiles = new ArrayList<>();
        try {
            StringBuilder queryString = new StringBuilder(" select tv.* ");
            queryString.append(" from in_importedtablestemp tables ");
            queryString.append(" inner join IO io ");
            queryString.append(" on tables.ioid = io.ioid ");
            queryString.append(" inner join tableversion tv ");
            queryString.append(" on tables.tablevid = tv.tablevid ");
            queryString.append(" where io.ioid = :ioid ");

            listOfFiles = jpa.getTypedNativeResultList(queryString.toString(),
                    "ioid", io.getIoId());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return listOfFiles;
    }
    

    
    public static List<TableVersionDPM> getAllVersionsOfATableVersion(String code) {
        if (code == null) {
            return null;
        }
        JPA<TableVersionDPM> jpa = new JPA<TableVersionDPM>(TableVersionDPM.class);
        List<TableVersionDPM> listOfFiles = new ArrayList<>();
        try {
            StringBuilder queryString = new StringBuilder("Select c.* from tableversion c  ");
            queryString.append(" where c.code = :code");

            listOfFiles = jpa.getTypedNativeResultList(queryString.toString(),
                    "code", code);

        } catch (Exception e) {
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
            List<Object[]> results = jpa.getFileQueryResultList("PropertiesIdentifier.sql",
                    "tableVId", String.valueOf(tableVId),
                    "referenceDate", referenceDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS).toString());
            
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

}