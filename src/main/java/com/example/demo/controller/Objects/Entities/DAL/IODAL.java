package com.example.demo.controller.Objects.Entities.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.Data.Connection;
import com.example.demo.Data.ConnectionManager;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.Conf.*;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.IO.IO;

public class IODAL {
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
            StringBuilder queryString = new StringBuilder("Select io.* from DPM_OD.IO io ");
            queryString.append(" inner join DPM_OD.io_state ioe on ioe.io_stateid = io.io_stateid ");
            queryString.append(" where referencedate = to_date(?referenceDate,?format) ");
            queryString.append(" and domain = ?domain ");
            queryString.append(" and entityId = ?entityId ");
            queryString.append(" and moduleVID = ?moduleVID ");
            queryString.append(" and ioe.io_typestateid = ?typeStatePending ");
            queryString.append(" and actionId <> ?actionIgnored ");
            result = jpa.getTypedNativeResultList(queryString.toString(),
                    "referenceDate", referenceDate,
                    "format",Constants.ISOBASEFORMAT8601,
                    "domain", domain,
                    "entityId", entity.getEntityID(),
                    "typeStatePending", Constants.tipoStatePending,
                    "moduleVID", module.getModuleVID(),
                    "actionIgnored", Constants.actionIgnore);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
    }

    public static List<IO> getIOsByAction(Integer moduleVID, LocalDate referenceDate, Integer entityID, String domain, short actionId) {

        List<IO> listOfGenerateLogs = new ArrayList<>();
        JPA<IO> jpa = new JPA<IO>(IO.class);

        try {
            domain = domain != null ? domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase() : null;

            StringBuilder queryString = new StringBuilder("select io.* from DPM_OD.io ");
            queryString.append(" where actionid = ?actionId ");
            queryString.append(" and (io.referencedate = ?referenceDate OR ?referenceDate IS NULL) ");
            queryString.append(" and (io.entityid = ?entityID OR ?entityID IS NULL) ");
            queryString.append(" and (io.domain = ?domain OR ?domain IS NULL)  ");
            queryString.append(" and (modulevid = ?moduleVId OR ?moduleVId IS NULL) ");
            queryString.append(" and io.io_stateid <> ?processOkDeleted ");
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

    public static List<IO> getIOsByActionAndModule(Integer moduleVID, short actionId) {

        List<IO> listOfGenerateLogs = new ArrayList<>();
        JPA<IO> jpa = new JPA<IO>(IO.class);

        try {

            StringBuilder queryString = new StringBuilder("select io.* from DPM_OD.io ");
            queryString.append(" where actionid = ?actionId ");
            queryString.append(" and (modulevid = ?moduleVId OR ?moduleVId IS NULL) ");
            queryString.append(" order by io.ioid desc");
            //queryString.append(!triggeredByUser ? " FETCH FIRST 25 ROWS ONLY " : "");

            listOfGenerateLogs = jpa.getMappedQueryResultList(queryString.toString(), IO.class,
                    "actionId", String.valueOf(actionId),
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
