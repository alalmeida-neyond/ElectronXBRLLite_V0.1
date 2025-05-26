package com.example.demo.controller.Objects.ActionPhases;

import com.example.demo.Data.Access.JPA;

public class DeleteAction {

    public void deleteRecords()
    {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        
        StringBuilder query = new StringBuilder(" DELETE FROM IN_IMPORTEDTABLESTEMP ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        } 

        query = new StringBuilder(" DELETE FROM IN_IMPORTEDVALUESTEMP ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        } 

        query = new StringBuilder(" DELETE FROM IN_KEYASSOCIATION ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        }  

        query = new StringBuilder(" DELETE FROM IO ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        }

        query = new StringBuilder(" DELETE FROM OUT_VALIDATIONRESULT ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        }

        query = new StringBuilder(" DELETE FROM OUT_VALIDATIONRESULTDETAILS ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        }

        query = new StringBuilder(" DELETE FROM OUT_VALIDATIONSDASHBOARD ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        } 

        query = new StringBuilder(" DELETE FROM OUT_VALIDATIONTABLE ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        } 

        query = new StringBuilder(" DELETE FROM OUT_VALIDATIONTABLERESULT ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        } 

        query = new StringBuilder(" DELETE FROM OUT_XBRLGENERATED ");
        
        try {
            jpa.executeNativeQuery(query.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            jpa.rollback();
        } finally {
            jpa.close();
        }
    }
    
}
