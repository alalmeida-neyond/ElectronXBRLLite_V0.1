package com.example.demo.controller.Objects.Entities.DAL;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.Data.Connection;
import com.example.demo.Data.ConnectionManager;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.controller.Objects.Entities.Conf.ConfEntities;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.IO.IO;

public class OperationRunningDAL {

    public static List<IO> getOperationRunningFromIO(ModuleVersion module, String domain, ConfEntities entity, String referenceDate) {

        domain = ( domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase());

        JPA<IO> jpa = new JPA<IO>(IO.class);
        List<IO> result = new ArrayList();

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
                    "format",Constants.ISOBASEFORMAT,
                    "domain", domain,
                    "entityId", String.valueOf(entity.getEntityID()),
                    "typeStatePending", String.valueOf(Constants.tipoStatePending),
                    "moduleVID", String.valueOf(module.getModuleVID()),
                    "actionIgnored", Constants.actionIgnore);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
    }

    public static boolean createNewOperationRunning(OperationsRunning operation){
        ConnectionManager cm = new ConnectionManager();
        boolean result = Connection.persist(cm, operation);
        cm.close();
        return result;
    }
    
    
    public static boolean createNewOperationRunning(String threadName, String operation, Integer moduleVID, String domain, Integer entity, String referenceDate, String userid) {
        ConnectionManager cm = new ConnectionManager();
        try {
            cm.em.getTransaction().begin();
            OperationsRunning oppRunning = new OperationsRunning();
            oppRunning.setDomain(domain);
            oppRunning.setUserId(userid);
            oppRunning.setThreadName(threadName);
            oppRunning.setOperation(operation);
            oppRunning.setRefDate(referenceDate);
            oppRunning.setEntity(cm.em.getReference(ConfEntities.class, entity));
            oppRunning.setModuleVersion(cm.em.getReference(ModuleVersion.class, moduleVID));
            cm.em.persist(oppRunning);
            cm.em.flush();
            cm.em.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            cm.em.close();
        }

        return true;
    }


}