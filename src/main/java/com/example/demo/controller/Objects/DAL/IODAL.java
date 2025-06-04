package com.example.demo.controller.Objects.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

import com.example.demo.Data.Connection;
import com.example.demo.Data.ConnectionManager;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.Conf.*;
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

    

    public static List<IO> getOperationRunningFromIO(ModuleVersion module, String domain, ConfEntities entity, String referenceDate) {

        JPA<IO> jpa = new JPA<IO>(IO.class);
        List<IO> result = new ArrayList<IO>();

        try {
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
    
}
