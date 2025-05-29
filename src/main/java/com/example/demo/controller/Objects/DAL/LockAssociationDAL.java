/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.DAL;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.Entities.IO;
import com.example.demo.controller.Objects.Lock.LockAssociation;

public class LockAssociationDAL {

    public static LockAssociation getLockFromGeneration(IO ioGen) {
        JPA<LockAssociation> jpa = new JPA<LockAssociation>(LockAssociation.class);
        List<LockAssociation> listOfResults = new ArrayList<>();
        try {
            StringBuilder query = new StringBuilder(" SELECT * LOCKASSOCIATION ");
            query.append(" WHERE IOIDGENERATE = ?ioIdLock");

            listOfResults = jpa.getTypedNativeResultList(query.toString(),
                    "ioIdLock", ioGen.getIoId());

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            jpa.close();
        }
     return listOfResults.get(0);
    }
    
    public static void deleteLockAssoc(LockAssociation lockAssoc) {
        JPA<LockAssociation> jpa = new JPA<LockAssociation>(LockAssociation.class);
        
        StringBuilder query = new StringBuilder(" DELETE FROM LOCKASSOCIATION ");
        query.append(" WHERE LOCKASSOCIATIONID = ?lockAssocId ");
        
        try {
            jpa.executeNativeQuery(query.toString(), 
                    "lockAssocId", String.valueOf(lockAssoc.getLockAssociationId()));
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        } finally {
            jpa.close();
        }
    }
}
