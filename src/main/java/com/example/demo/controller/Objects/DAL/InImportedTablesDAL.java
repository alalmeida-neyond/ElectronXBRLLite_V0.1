/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.DAL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.example.demo.DTOs.*;
import com.example.demo.Data.Connection;
import com.example.demo.Data.Access.*;
import com.example.demo.controller.Objects.ConfEntities;
import com.example.demo.controller.Objects.Constants;
import com.example.demo.controller.Objects.Info;
import com.example.demo.controller.Objects.Utils;
import com.example.demo.controller.Objects.ActionPhases.ValidationAction;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.controller.Objects.Import.*;

import jakarta.persistence.EntityManager;

public class InImportedTablesDAL {
    public static void createEmptyMap(List<ImportedTablesWithLockDTO> importedTables, String userId) {
        JPA<InImportedTablesTemp> jpa = new JPA<>(InImportedTablesTemp.class);
        IO io = null;
        List<InImportedTablesTemp> importedTablesEmpty = new ArrayList<>();
        try {
            for (ImportedTablesWithLockDTO impTable : importedTables) {
                io = new IO(Info.getInstance().getIOStateByID(Constants.processoOkDeleted),
                        //new IOState(Constants.processoOkDeleted, new IOTypeState(Constants.tipoStateOK)),
                        impTable.getImportedTable().getIo().getReferenceDate(),
                        impTable.getImportedTable().getIo().getModule(),
                        impTable.getImportedTable().getIo().getDomain(),
                        impTable.getImportedTable().getIo().getEntity(),
                        LocalDateTime.now(), LocalDateTime.now(),
                        Info.getInstance().getConfActionByID(Integer.valueOf(Constants.actionImport)),//new ConfAction(Constants.actionImport),
                        userId,
                        impTable.getImportedTable().getIo().getFilename(),
                        impTable.getImportedTable().getIo().getThreadFilename(),
                        impTable.getImportedTable().getIo().getFilenameserver()
                );
                Connection.persist(jpa.getEm(), io);
                
                InImportedTablesTemp impTableAux = impTable.getImportedTable();
                InImportedTablesTemp importedTableEmpty = new InImportedTablesTemp(
                        io, 
                        impTableAux.getTableVersion(), 
                        impTableAux.getVariableVersion(), 
                        impTableAux.getImportKey() , 
                        impTableAux.getInitTimestamp(), 
                        impTableAux.getEndTimestamp(), 
                        impTableAux.getIoState(), 
                        impTableAux.getDesagregationCode());
                Connection.persist(jpa.getEm(), importedTableEmpty);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
    }

    public static List<InImportedTablesTemp> getListOfImportedMaps(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain, IO io) {
        JPA<InImportedTablesTemp> jpa = new JPA<>(InImportedTablesTemp.class);
        domain = domain != null ? (domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain) : null;
        String queryStr = Utils.getResource("GetImportedTablesForGeneration.sql");
        //queryStr = queryStr.concat(!triggeredByUser ? " FETCH FIRST 25 ROWS ONLY " : "");
        
        List<InImportedTablesTemp> listOfMaps = new ArrayList<>();
        try {
            /*listOfMaps = jpa.getTypedNativeResultList(queryStr,
                    "processOkDeleted", String.valueOf(Constants.processoOkDeleted),
                    "actionId", String.valueOf(Constants.actionImport),
                    "actionGenerateId", String.valueOf(Constants.actionGeneration),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "desagregationCodeType", String.valueOf(Constants.DESAGREGATIONCODETYPE),
                    "desagregationCodeFixedType", String.valueOf(Constants.DESAGREGATIONCODEFIXEDTYPE),
                    "referenceDate", referenceDate != null ? referenceDate.format(Constants.dateFormat): null,
                    "format",Constants.ISOBASEFORMATSQlite,
                    "domain", domain != null ? domain.toUpperCase() : null,
                    "moduleVID", module != null ? String.valueOf(module.getModuleVID()) : null,
                    "entityId", entity != null ? String.valueOf(entity.getEntityID()) : null);*/
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
            /*listOfMaps = jpa.getTypedNativeResultList(queryStr,
                    "actionId", String.valueOf(Constants.actionImport),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "referenceDate", referenceDate.format(Constants.DATEFORMATISO8601),
                    "format",Constants.ISOBASEFORMAT8601,
                    "domain", domain.toUpperCase(),
                    "moduleVID", String.valueOf(module.getModuleVID()),
                    "entityId", String.valueOf(entity.getEntityID()),
                    "stateOk", String.valueOf(Constants.processoOk),
                    "processOkDeleted", String.valueOf(Constants.processoOkDeleted));*/
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

    public static List<ImportedTablesWithLockDTO> getListOfImportedMapsWithLock(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain) {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        domain = domain != null ? (domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain) : null;
        List<ImportedTablesWithLockDTO> importedTablesWithLock = new ArrayList<>();
        List<Object[]> listOfMaps = new ArrayList<>();
        String queryStr = Utils.getResource("GetImportedTablesWithLock.sql");
        //queryStr = queryStr.concat(!triggeredByUser ? " FETCH FIRST 25 ROWS ONLY " : "");
        try {

            listOfMaps = jpa.getNativeResultListWithMapping(queryStr, "TablesWithLock",
                    "processOkDeleted", String.valueOf(Constants.processoOkDeleted),
                    "actionId", String.valueOf(Constants.actionImport),
                    "actionGenerateId", String.valueOf(Constants.actionGeneration),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "desagregationCodeType", String.valueOf(Constants.DESAGREGATIONCODETYPE),
                    "desagregationCodeFixedType", String.valueOf(Constants.DESAGREGATIONCODEFIXEDTYPE),
                    "referenceDate", referenceDate != null ? referenceDate.format(Constants.dateFormat) : null,
                    "format",Constants.ISOBASEFORMAT,
                    "domain", domain != null ? domain.toUpperCase() : null,
                    "moduleVID", module != null ? String.valueOf(module.getModuleVID()) : null,
                    "entityId", entity != null ? String.valueOf(entity.getEntityID()) : null);

            for (Object[] obj : listOfMaps) {
                importedTablesWithLock.add(new ImportedTablesWithLockDTO((InImportedTablesTemp) obj[0], obj[1] == null ? false : true));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return importedTablesWithLock;
    }

    public static List<Object[]> getImportedTableByTableVIDAndDesagregationCode(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain) {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        domain = domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain;

        List<Object[]> listOfMaps = new ArrayList<>();
        try {
            listOfMaps = jpa.getNativeResultList(Utils.getResource("GetImportedTablesByTableVIDAndDesagregationCode.sql"),
                    "actionGenerateId", String.valueOf(Constants.actionGeneration),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "referenceDate", referenceDate.format(Constants.dateFormat),
                    "format",Constants.ISOBASEFORMAT,
                    "actionId", String.valueOf(Constants.actionImport),
                    "desagregationCodeType", String.valueOf(Constants.DESAGREGATIONCODETYPE),
                    "domain", domain.toUpperCase(),
                    "moduleVID", String.valueOf(module.getModuleVID()),
                    "entityId", String.valueOf(entity.getEntityID()));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfMaps;
    }

    public static void smashPrevious(List<Object[]> importedTables, TableVersionDPM table, InImportKey desagregationCode) {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        String desagCodeFixed = null;
        if (desagregationCode != null) {
            desagCodeFixed = String.join("|", desagregationCode.getListPropertyValues().stream()
                    .sorted(Comparator.comparing(b -> b.getPropertyValue())).map(InKeyAssociation::getPropertyValue)
                    .collect(Collectors.toList()));
        }
        try {

            int importedTableId = -1;
            for (Object[] impTable : importedTables) {
                if (Integer.valueOf(impTable[1].toString()) == table.getTableVID()) {
                    if (desagCodeFixed == null) {
                        importedTableId = Integer.valueOf(impTable[0].toString());
                        break;
                    } else {
                        if (impTable[2].toString().trim().equals(desagCodeFixed)) {
                            importedTableId = Integer.valueOf(impTable[0].toString());
                            break;
                        }
                    }
                }
            }

            if (importedTableId == -1) {
                return;
            } else {
                String deleteFromInImportedValues = "Delete from IN_IMPORTEDVALUESTEMP where importedtableid = " + String.valueOf(importedTableId);
                
                String deleteFromInImportedTable = "Delete from IN_IMPORTEDTABLESTEMP where importedtableid = " + String.valueOf(importedTableId);

                //LogImportProcess smashLog = new LogImportProcess(importedTableId, Constants.IMPORTSMASHLOGMESSAGE);
                                
                EntityManager em = jpa.getEm().em;
                em.getTransaction().begin();
                em.createNativeQuery(deleteFromInImportedValues).executeUpdate();
                em.createNativeQuery(deleteFromInImportedTable).executeUpdate();
                //em.persist(smashLog);
                em.getTransaction().commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
    }

    public static boolean hasDuplicatedValuesTemp(Integer ioid) {
        JPA<BigDecimal> jpa = new JPA<BigDecimal>(BigDecimal.class);
        BigDecimal count = null;
        try {
            StringBuilder queryString = new StringBuilder("SELECT CASE WHEN EXISTS ( ");
            queryString.append(" SELECT 1 ");
            queryString.append(" FROM IN_IMPORTEDVALUESTEMP a ");
            queryString.append(" INNER JOIN IN_IMPORTEDTABLESTEMP b ");
            queryString.append(" on b.importedtableid = a.importedtableid ");
            queryString.append(" WHERE b.ioid = ?ioId ");
            queryString.append(" GROUP BY a.IMPORTEDTABLEID, a.IMPORTEDVALUE, a.IMPORTKEYID, a.VARIABLEVID ");
            queryString.append(" HAVING COUNT(*) > 1 ");
            queryString.append(" ) THEN 1 ELSE 0 END ");
            queryString.append(" FROM DUAL");

            count = jpa.getTypedNativeResult(queryString.toString(),
                    "ioId", ioid);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            jpa.close();
        }
        return count == null ? false : (count.equals(0) ? false : true);
    }
    
    public static List<ImportedFilesResumeDTO> getImportedFilesResume(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain){
        JPA<ImportedFilesResumeDTO> jpa = new JPA<>(ImportedFilesResumeDTO.class);
        List<ImportedFilesResumeDTO> importedFilesResumeList = new ArrayList<>();
        domain = domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain;
        String queryStr = Utils.getResource("GetImportedFilesResume.sql");
        try {
            importedFilesResumeList = jpa.getMappedFileQueryResultList("GetImportedFilesResume.sql", "ImportedFilesResumeRow", 
                    "importActionId", String.valueOf(Constants.actionImport),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "localDateFormat", Constants.DATEFORMATISO8601STRING,
                    "processoOk", Constants.processoOk,
                    "processoOkDeleted", Constants.processoOkDeleted,
                    "referenceDate", referenceDate != null ? referenceDate.format(Constants.DATEFORMATISO8601) : null,
                    "moduleVId", module != null ? String.valueOf(module.getModuleVID()) : null,
                    "entityId", entity != null ? String.valueOf(entity.getEntityID()) : null,
                    "domain", domain);
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            jpa.close();
        }
        return importedFilesResumeList;
    }
	
    public static List<InImportedTablesTemp> getListOfImportedMapsFromLock(IO io) {
        JPA<InImportedTablesTemp> jpa = new JPA<>(InImportedTablesTemp.class);
        List<InImportedTablesTemp> listOfMaps = new ArrayList<>();
        try {
            StringBuilder queryString = new StringBuilder("Select it.* from in_importedtablestemp it ");
            queryString.append(" inner join lockimportassociation lia on lia.importedtableid = it.importedtableid ");
            queryString.append(" inner join lockassociation la on la.lockassociationid = lia.lockassociationid ");
            queryString.append(" where la.IOIDGENERATE = :IOGENERATE ");

            listOfMaps = jpa.getTypedNativeResultList(queryString.toString(),
                                                    "IOGENERATE", io.getIoId());
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