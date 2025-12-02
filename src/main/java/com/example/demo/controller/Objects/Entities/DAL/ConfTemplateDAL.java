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

        StringBuilder queryString = new StringBuilder("Select a.* from DPM_OD.Conf_Template a ");
        queryString.append(" inner join DPM_MD.moduleversion b on a.TemplateID = b.moduleVID ");
        queryString.append(" inner join DPM_MD.release r on r.releaseid = b.STARTRELEASEID ");
        queryString.append(" where b.fromreferencedate<>NVL(b.TOREFERENCEDATE,to_date('99991231','YYYYMMDD')) ");
            queryString.append(" and (b.moduleid = ?moduleID or ?moduleID is null) ");
            queryString.append(" and (r.code = ?version or ?version is null) ");
            queryString.append(" and (b.fromreferencedate <= to_date(?referenceDate , ?format) ");
            queryString.append(" and (b.TOREFERENCEDATE is null or NVL(b.TOREFERENCEDATE, to_date('99991231','YYYYMMDD')) > to_date(?referenceDate,?format)) ");
            queryString.append(" or  ?referenceDate IS NULL)");
        queryString.append(" Order by TO_NUMBER(REGEXP_SUBSTR(r.code, '^[0-9]+', 1, 1)) desc, ");
        queryString.append(" TO_NUMBER(REGEXP_SUBSTR(r.code, '[0-9]+', 1, 2)) desc, b.code");

        try {

            result = jpa.getTypedNativeResultList(queryString.toString(),
                                                "moduleID",moduleID,
                                                "version",version,
                                                "format",Constants.ISOBASEFORMAT,
                                                "referenceDate",referenceDate!=null? referenceDate.format(Constants.dateFormat):null);

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
            result = jpa.getTypedNativeResult("Select jsonfilename from DPM_OD.Conf_Template where templateID = ?moduleVID",
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
            result = jpa.getTypedNativeResult("Select entryPointUrl from DPM_OD.Conf_Template where templateID = ?moduleVID",
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
            jpa.executeNativeQuery("Update DPM_OD.Conf_Template set SERVERFILENAME = ?filename where TEMPLATEID = ?templateID",
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
            jpa.executeNativeQuery("Update DPM_OD.Conf_Template set EntryPointURL = ?filename where TEMPLATEID = ?templateID",
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
