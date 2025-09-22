package com.example.demo.controller.Objects.Entities.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.Data.ConnectionManager;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.Conf.ConfTemplate;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;

public class ConfTemplateDAL {

    public static List<ConfTemplate> getTemplates(String moduleID, LocalDate referenceDate, String version) {

        JPA<ConfTemplate> jpa = new JPA<ConfTemplate>(ConfTemplate.class);
        List<ConfTemplate> result = new ArrayList();

        StringBuilder queryString = new StringBuilder("Select a.* from Conf_Template a ");
        queryString.append(" inner join moduleversion b on a.TemplateID = b.moduleVID ");
        queryString.append(" where 1=1 ");
        if (moduleID != null) {
            queryString.append(" and b.moduleid = " + moduleID);
        }
        if (version != null) {
            queryString.append(" and b.versionnumber = '" + version +"'");
        }

        if (referenceDate != null) {
            queryString.append(" and b.fromreferencedate <= strftime('YYYYMMDD'),"+referenceDate.format(Constants.dateFormat)+" ");
            queryString.append(" and (b.TOREFERENCEDATE is null or b.TOREFERENCEDATE > strftime('YYYYMMDD'),"+referenceDate.format(Constants.dateFormat)+") ");

        }
        queryString.append(" Order by TO_NUMBER(REGEXP_SUBSTR(b.VERSIONNUMBER, '^[0-9]+', 1, 1)) desc, ");
        queryString.append(" TO_NUMBER(REGEXP_SUBSTR(b.VERSIONNUMBER, '[0-9]+', 1, 2)) desc, ");
        queryString.append(" TO_NUMBER(REGEXP_SUBSTR(b.VERSIONNUMBER, '[0-9]+', 1, 3)) desc, b.code");

        try {

            result = jpa.getTypedNativeResultList(queryString.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }

        return result;
    }

    public static String getJSONFile(int moduleVID) {

        JPA<String> jpa = new JPA<String>(String.class);
        String result = null;

        try {
            result = jpa.getTypedNativeResult("Select jsonfilename from Conf_Template where templateID = :moduleVID",
                    "moduleVID", String.valueOf(moduleVID));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
        return result;
    }
    public static String getEntryPointURL(int moduleVID) {
        JPA<String> jpa = new JPA<String>(String.class);
        String result = null;

        try {
            result = jpa.getTypedNativeResult("Select entryPointUrl from Conf_Template where templateID = :moduleVID",
                    "moduleVID", String.valueOf(moduleVID));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
        return result;
    }

    public static boolean updateTemplate(int templateID, String filename) {
        JPA<Object> jpa = new JPA<Object>(Object.class);
        try {
            jpa.executeNativeQuery("Update Conf_Template set SERVERFILENAME = :filename where TEMPLATEID = :templateID",
                    "filename", filename,
                    "templateID", String.valueOf(templateID));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            jpa.close();
        }
        return true;
    }

    public static boolean updateJSON(int templateID, String entryPointURL) {
        JPA<Object> jpa = new JPA<Object>(Object.class);
        try {
            jpa.executeNativeQuery("Update Conf_Template set EntryPointURL = :filename where TEMPLATEID = :templateID",
                    "filename", entryPointURL,
                    "templateID", String.valueOf(templateID));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            jpa.close();
        }
        return true;
    }

    public static boolean createNewTemplate(int templateID, String serverFileName, String fileName, String EntryPointURL) {
        ConnectionManager cm = new ConnectionManager();
        try {
            cm.em.getTransaction().begin();
            ConfTemplate template = new ConfTemplate();
            template.setTemplateID(templateID);
            template.setServerFilename(serverFileName);
            template.setFilename(fileName);
            template.setEntryPointURL(EntryPointURL);
            template.setModuleVersion(cm.em.getReference(ModuleVersion.class, templateID));
            cm.em.persist(template);
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
