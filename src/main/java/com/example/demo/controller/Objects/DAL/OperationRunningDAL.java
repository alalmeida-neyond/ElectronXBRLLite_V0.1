package com.example.demo.controller.Objects.DAL;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.Data.Connection;
import com.example.demo.Data.ConnectionManager;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.ConfEntities;
import com.example.demo.controller.Objects.Constants;
import com.example.demo.controller.Objects.Entities.*;

public class OperationRunningDAL {

    public static List<IO> getOperationRunningFromIO(ModuleVersion module, String domain, ConfEntities entity, String referenceDate) {

        domain = ( domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase());

        JPA<IO> jpa = new JPA<IO>(IO.class);
        List<IO> result = new ArrayList<IO>();

        try {
            /*StringBuilder queryString = new StringBuilder("Select io.* from IO io ");
            queryString.append(" inner join io_state ioe on ioe.io_stateid = io.io_stateid ");
            queryString.append(" where referencedate = :referenceDate ");
            queryString.append(" and domain = :domain ");
            queryString.append(" and entityId = :entityId ");
            queryString.append(" and moduleVID = :moduleVID ");
            queryString.append(" and ioe.io_typestateid = :typeStatePending ");*/

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
    /*public static List<OperationsRunning> getOperationOfCurrentUser(String operation, String userid) {

        userid = Utils.getStringOrOperatorPercentage(userid);

        JPA<OperationsRunning> jpa = new JPA<OperationsRunning>(OperationsRunning.class);
        List<OperationsRunning> result = new ArrayList<OperationsRunning>();
        try {

            result = jpa.getTypedNativeResultList("Select * from operationrunning "
                    + "where upper(operation) like ?operation and userid like ?userid",
                    "operation", operation.toUpperCase(),
                    "userid", userid);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
    }*/

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

    public static boolean deleteOperationRun(String threadName) {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        try {
            jpa.executeNativeQuery("Delete operationrunning where threadname = ?threadName",
                    "threadName", threadName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            jpa.close();
        }

        return true;
    }

}