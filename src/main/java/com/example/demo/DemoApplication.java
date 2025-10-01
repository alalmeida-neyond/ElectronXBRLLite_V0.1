package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import com.example.config.ReplacementDB;
import com.example.demo.Data.Access.JPA;

@SpringBootApplication
public class DemoApplication {
	

	public static void main(String[] args) {
        ReplacementDB replacementDB = new ReplacementDB();
		replacementDB.replacementDBEvent();
		
		if (!replacementDB.isValidSQLiteFile()) {
			System.err.println("The SQLite file is invalid. Application will not start.");
			System.exit(1);
		}
        //ATIVAR EM PROD
        //deleteRecords();

        //Info.getInstance().loadRefData(true);
		SpringApplication.run(DemoApplication.class, args);
	}

	public static void deleteRecords()
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
