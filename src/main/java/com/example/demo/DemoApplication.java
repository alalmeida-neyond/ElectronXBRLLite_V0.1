package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class DemoApplication extends SpringBootServletInitializer{
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DemoApplication.class);
    }

	public static void main(String[] args) {
        //ATIVAR EM PROD
        //deleteRecords();

        //Info.getInstance().loadRefData(true);
		SpringApplication.run(DemoApplication.class, args);
	}

	/*public static void deleteRecords()
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

        query = new StringBuilder(" DELETE FROM IN_IMPORTKEY where IMPORTKEYID != -1 ");
        
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
    }*/
}
