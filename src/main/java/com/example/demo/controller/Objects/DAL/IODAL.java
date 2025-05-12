package com.example.demo.controller.Objects.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

import com.example.demo.Data.Connection;
import com.example.demo.Data.ConnectionManager;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.ConfEntities;
import com.example.demo.controller.Objects.Constants;
import com.example.demo.controller.Objects.Entities.IO;
import com.example.demo.controller.Objects.Entities.ModuleVersion;
import com.example.demo.controller.Objects.Import.InImportedTablesTemp;

public class IODAL {

    private final static Logger LOG = Logger.getLogger(IODAL.class);

    public static IO persist(IO io) throws Exception {
        Connection.persist(io);
        return io;
    }

    public static IO persist(ConnectionManager em, IO io) throws Exception {
        Connection.persist(em, em.tx, io);
        return io;
    }

    public static IO persistAndCommit(ConnectionManager em, IO io) throws Exception {
        Connection.persist(em, io);
        return io;
    }
    
    public static <T> void persistAndCommitList(ConnectionManager em, List<T> lst) throws Exception {
        Connection.persistList(em, lst);
    }

    public static List<InImportedTablesTemp> getImportedFilesByModuleEntityDomaindAndReferenceDate(Integer moduleID, Integer entityID, String referenceDate, String domain) {
        JPA<InImportedTablesTemp> jpa = new JPA<>(InImportedTablesTemp.class);
        List<InImportedTablesTemp> listOfFiles = new ArrayList<>();
        try {

            domain = domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain;

            /*StringBuilder queryString = new StringBuilder("with importedIOs as ( ");
            queryString.append("    select io.ioid, io.moduleVID, io.referenceDate, io.domain, io.entityId ");
            queryString.append("    from IO io ");
            queryString.append("    inner join io_state ioe on ioe.io_stateid = io.io_stateid  ");
            queryString.append("    where referencedate = :referenceDate  ");
            queryString.append("    and domain = :domain  ");
            queryString.append("    and entityId = :entityId  ");
            queryString.append("    and moduleVID = :moduleVID ");
            queryString.append(") ");
            queryString.append(", maxImportedTableIdByTableVID as ( ");
            queryString.append("    select max(impTable.importedTableId) importedTableId, impTable.tablevid, impTable.importKeyId ");
            queryString.append("    from in_importedtablestemp impTable ");
            queryString.append("    inner join importedIOs impIo on impIO.ioid = impTable.ioid ");
            queryString.append("    left join in_importkey impKey on impKey.importKeyId = impTable.importKeyId ");
            queryString.append("    left join in_keytype keyType on keyType.keyTypeId = impKey.keyTypeId ");
            queryString.append("    where keyType.keyTypeId is null or keyType.keyTypeId = :desagregationCodeType ");
            queryString.append("    group by impTable.tablevid, impTable.importKeyId ");
            queryString.append(") ");
            queryString.append("select impTable.* ");
            queryString.append("from in_importedtablestemp impTable ");
            queryString.append("inner join maxImportedTableIdByTableVID maxImportedId on maxImportedId.importedTableId = impTable.importedTableId ");*/

            StringBuilder queryString = new StringBuilder("with importedIOs as ( ");
            queryString.append("    select io.ioid, io.moduleVID, io.referenceDate, io.domain, io.entityId ");
            queryString.append("    from IO io ");
            queryString.append("    inner join io_state ioe on ioe.io_stateid = io.io_stateid  ");
            queryString.append("    where datetime(referencedate / 1000, 'unixepoch') = :referenceDate  ");
            queryString.append("    and domain = :domain  ");
            queryString.append("    and entityId = :entityId  ");
            queryString.append("    and moduleVID = :moduleVID ");
            queryString.append(") ");
            queryString.append(", maxImportedTableIdByTableVID as ( ");
            queryString.append("    select max(impTable.importedTableId) importedTableId, impTable.tablevid, impTable.importKeyId ");
            queryString.append("    from in_importedtablestemp impTable ");
            queryString.append("    inner join importedIOs impIo on impIO.ioid = impTable.ioid ");
            queryString.append("    left join in_importkey impKey on impKey.importKeyId = impTable.importKeyId ");
            queryString.append("    left join in_keytype keyType on keyType.keyTypeId = impKey.keyTypeId ");
            queryString.append("    where keyType.keyTypeId is null or keyType.keyTypeId = :desagregationCodeType ");
            queryString.append("    group by impTable.tablevid, impTable.importKeyId ");
            queryString.append(") ");
            queryString.append("select impTable.* ");
            queryString.append("from in_importedtablestemp impTable ");
            queryString.append("inner join maxImportedTableIdByTableVID maxImportedId on maxImportedId.importedTableId = impTable.importedTableId ");

            listOfFiles = jpa.getTypedNativeResultList(queryString.toString(),
                    "desagregationCodeType", Constants.DESAGREGATIONCODETYPE,
                    "referenceDate", referenceDate,
                    "entityId", String.valueOf(entityID),
                    "moduleVID", String.valueOf(moduleID),
                    "typeStatePending", String.valueOf(Constants.processoOk),
                    "domain", domain);

        } catch (Exception e) {
            LOG.error("Erro na obtenção dos IOs relativos à importação do módulo: " + moduleID + ", entity: " + entityID + ", referenceDate: " + referenceDate + ", domain: " + domain + ".", e);
        } finally {
            jpa.close();
        }

        return listOfFiles;
    }

    public static List<IO> getOperationRunningFromIO(ModuleVersion module, String domain, ConfEntities entity, String referenceDate) {

        JPA<IO> jpa = new JPA<IO>(IO.class);
        List<IO> result = new ArrayList<IO>();

        try {
            /*StringBuilder queryString = new StringBuilder("Select io.* from IO io ");
            queryString.append(" inner join io_state ioe on ioe.io_stateid = io.io_stateid ");
            queryString.append(" where referencedate = :referenceDate ");
            queryString.append(" and domain = :domain ");
            queryString.append(" and entityId = :entityId ");
            queryString.append(" and moduleVID = :moduleVID ");
            queryString.append(" and ioe.io_typestateid = :typeStatePending ");
            result = jpa.getTypedNativeResultList(queryString.toString(),
                    "referenceDate", referenceDate,
                    "domain", domain,
                    "entityId", String.valueOf(entity.getEntityID()),
                    "typeStatePending", String.valueOf(Constants.tipoStatePending),
                    "moduleVID", String.valueOf(module.getModuleVID()));*/

            StringBuilder queryString = new StringBuilder("Select io.* from IO io ");
            queryString.append(" inner join io_state ioe on ioe.io_stateid = io.io_stateid ");
            queryString.append(" where datetime(referencedate / 1000, 'unixepoch') = :referenceDate ");
            queryString.append(" and domain = :domain ");
            queryString.append(" and entityId = :entityId ");
            queryString.append(" and moduleVID = :moduleVID ");
            queryString.append(" and ioe.io_typestateid = :typeStatePending ");
            result = jpa.getTypedNativeResultList(queryString.toString(),
                    "referenceDate", referenceDate,
                    "domain", domain,
                    "entityId", String.valueOf(entity.getEntityID()),
                    "typeStatePending", String.valueOf(Constants.tipoStatePending),
                    "moduleVID", String.valueOf(module.getModuleVID()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
    }

    public static List<IO> getIOsByAction(Integer moduleVID, LocalDate referenceDate, Integer entityID, String domain, short actionId, Integer privilegio) {

        List<IO> listOfGenerateLogs = new ArrayList<>();
        JPA<IO> jpa = new JPA<IO>(IO.class);

        try {
            domain = domain != null ? domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase() : null;

            /*StringBuilder queryString = new StringBuilder("select io.* from io ");
            queryString.append(" where actionid = :actionId ");
            queryString.append(" and (io.referencedate = :referenceDate OR :referenceDate IS NULL) ");
            queryString.append(" and (io.entityid = :entityID OR :entityID IS NULL) ");
            queryString.append(" and (io.domain = :domain OR :domain IS NULL)  ");
            queryString.append(" and (modulevid = :moduleVId OR :moduleVId IS NULL) ");
            queryString.append(" and io.io_stateid <> :processOkDeleted ");
            queryString.append(" order by io.ioid desc");
            queryString.append(!triggeredByUser ? " FETCH FIRST 25 ROWS ONLY " : "");*/

            StringBuilder queryString = new StringBuilder("select io.* from io ");
            queryString.append(" where actionid = :actionId ");
            queryString.append(" and (datetime(io.referencedate / 1000, 'unixepoch') = :referenceDate OR :referenceDate IS NULL) ");
            queryString.append(" and (io.entityid = :entityID OR :entityID IS NULL) ");
            queryString.append(" and (io.domain = :domain OR :domain IS NULL)  ");
            queryString.append(" and (modulevid = :moduleVId OR :moduleVId IS NULL) ");
            queryString.append(" and io.io_stateid <> :processOkDeleted ");
            queryString.append(" order by io.ioid desc");
            //queryString.append(!triggeredByUser ? " FETCH FIRST 25 ROWS ONLY " : "");

            listOfGenerateLogs = jpa.getMappedQueryResultList(queryString.toString(), IO.class,
                    "processOkDeleted", String.valueOf(Constants.processoOkDeleted),
                    "actionId", String.valueOf(actionId),
                    "referenceDate", referenceDate,
                    "entityID", entityID,
                    "domain", domain,
                    "moduleVId", moduleVID);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfGenerateLogs;
    }

    /*public static LazyDataModelNativeQuery<IO> getIOsByAction(Integer moduleVID, LocalDate referenceDate, Integer entityID, String domain, short actionId, Integer privilegio, String user, boolean admin) {
        LazyDataModelNativeQuery<IO> lazy = null;
        try {
            lazy = new LazyDataModelNativeQuery<IO>(null,
                    IO.class,
                    "GetIOsByAction.sql",
                    null,
                    null,
                    null,
                    null,
                    new HashMap<String, String>()
            );
            lazy.setParameters("processOkDeleted", String.valueOf(Constants.processoOkDeleted),
                    "actionId", String.valueOf(actionId),
                    "privilegioId", String.valueOf(privilegio),
                    "privilegioView", String.valueOf(Constants.VIEWID),
                    "userId", String.valueOf(user),
                    "admin",String.valueOf(admin ? 1 : 0),
                    "referenceDate", referenceDate,
                    "entityID", entityID,
                    "domain", domain,
                    "moduleVId", moduleVID);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lazy;
    }*/

    public static IO getIOById(String id) {
        JPA<IO> jpa = new JPA<IO>(IO.class);
        IO io = null;
        try {
            io = jpa.getSimpleResult("ioId", id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
        return io;
    }

    public static IO getIOValidation(Integer moduleVID, String referenceDate, Integer entityID, String domain) {
        JPA<IO> jpa = new JPA<IO>(IO.class);
        List<IO> results = new ArrayList<>();
        try {

            /*StringBuilder query = new StringBuilder(" select io.*  ");
            query.append(" from io io  ");
            query.append(" where io.ioId = (  ");
            query.append("     select max(io.ioId)  ");
            query.append("     from io io  ");
            query.append("     join io_state ioe on ioe.io_stateId = io.io_stateId  ");
            query.append("     where ioe.io_typeStateId = :typeStateOk  ");
            query.append("       and io.actionId = :actionIdValidate  ");
            query.append("       and io.referenceDate = :referenceDate  ");
            query.append("       and io.entityId = :entityId  ");
            query.append("       and io.domain = :domain  ");
            query.append("       and io.moduleVID = :moduleVID  ");
            query.append(" )  ");*/

            StringBuilder query = new StringBuilder(" select io.*  ");
            query.append(" from io io  ");
            query.append(" where io.ioId = (  ");
            query.append("     select max(io.ioId)  ");
            query.append("     from io io  ");
            query.append("     join io_state ioe on ioe.io_stateId = io.io_stateId  ");
            query.append("     where ioe.io_typeStateId = :typeStateOk  ");
            query.append("       and io.actionId = :actionIdValidate  ");
            query.append("       and datetime(io.referencedate / 1000, 'unixepoch') = :referenceDate  ");
            query.append("       and io.entityId = :entityId  ");
            query.append("       and io.domain = :domain  ");
            query.append("       and io.moduleVID = :moduleVID  ");
            query.append(" )  ");

            results = jpa.getMappedQueryResultList(query.toString(), IO.class,
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "actionIdValidate", String.valueOf(Constants.actionValidation),
                    "referenceDate", referenceDate,
                    "entityId", String.valueOf(entityID),
                    "domain", domain,
                    "moduleVID", String.valueOf(moduleVID));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
        return results.get(0);
    }
    
    public static IO getIOValidationForLock(IO ioGen) {
        JPA<IO> jpa = new JPA<IO>(IO.class);
        List<IO> results = new ArrayList<>();
        try {
            
            /*StringBuilder query = new StringBuilder(" select io.*  ");
            query.append(" from io io  ");
            query.append(" where io.ioId = (  ");
            query.append("     select max(io.ioId)  ");
            query.append("     from io io  ");
            query.append("     join io_state ioe on ioe.io_stateId = io.io_stateId  ");
            query.append("     where ioe.io_typeStateId = :typeStateOk  ");
            query.append("       and io.actionId = :actionIdValidate  ");
            query.append("       and io.referenceDate = :referenceDate  ");
            query.append("       and io.entityId = :entityId  ");
            query.append("       and io.domain = :domain  ");
            query.append("       and io.moduleVID = :moduleVID  ");
            query.append("       and io.ioid < :ioidGen  ");
            query.append(" )  ");*/

            StringBuilder query = new StringBuilder(" select io.*  ");
            query.append(" from io io  ");
            query.append(" where io.ioId = (  ");
            query.append("     select max(io.ioId)  ");
            query.append("     from io io  ");
            query.append("     join io_state ioe on ioe.io_stateId = io.io_stateId  ");
            query.append("     where ioe.io_typeStateId = :typeStateOk  ");
            query.append("       and io.actionId = :actionIdValidate  ");
            query.append("       and datetime(io.referencedate / 1000, 'unixepoch') = :referenceDate  ");
            query.append("       and io.entityId = :entityId  ");
            query.append("       and io.domain = :domain  ");
            query.append("       and io.moduleVID = :moduleVID  ");
            query.append("       and io.ioid < :ioidGen  ");
            query.append(" )  ");
            
            results = jpa.getMappedQueryResultList(query.toString(), IO.class,
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "actionIdValidate", String.valueOf(Constants.actionValidation),
                    "referenceDate", ioGen.getReferenceDate(),
                    "entityId", String.valueOf(ioGen.getEntity().getEntityID()),
                    "domain", ioGen.getDomain(),
                    "moduleVID", String.valueOf(ioGen.getModule().getModuleVID()),
                    "ioidGen", String.valueOf(ioGen.getIoId()));
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
             jpa.close();
        }
        return results.get(0);
    }

    /*public static DashboardGeralDTO getIOsInfoForDashboard(ModuleVersion module, String referenceDate, ConfEntities entity, String domain) {
        JPA<DashboardGeralDTO> jpa = new JPA<DashboardGeralDTO>(DashboardGeralDTO.class);
        List<DashboardGeralDTO> results = new ArrayList<>();
        try {

            results = jpa.getNativeResultListWithMapping(Utils.getResource("GetDashboardGeral.sql"), "DashboardGeralRow",
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "actionImportId", String.valueOf(Constants.actionImport),
                    "actionValidateId", String.valueOf(Constants.actionValidation),
                    "actionGenerateId", String.valueOf(Constants.actionGeneration),
                    "refDate", referenceDate,
                    "entityID", String.valueOf(entity.getEntityID()),
                    "domain", domain,
                    "moduleVID", String.valueOf(module.getModuleVID()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return results.get(0);
    }*/
    
}
