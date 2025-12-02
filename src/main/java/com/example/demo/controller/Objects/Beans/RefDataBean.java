package com.example.demo.controller.Objects.Beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.example.demo.Data.Access.Info;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;


@Component("refData")
@Scope("singleton")
public class RefDataBean {

    private HashMap<String, HashMap<Integer, List<String>>> privilegios;
    private Map<String, WebPage[]> webPageMap = new HashMap<>();
    private Map<String, Integer> monthsMap;
    private boolean importBoolean = false;
    private boolean validate = false;
    private boolean generate = false;    
    private boolean importBooleanOnly = false;
    private boolean validateOnly = false;
    private boolean generateOnly = false;   
    private boolean view = false;
    private boolean isAdmin = false;    
    
    public RefDataBean() {
    }

    @PostConstruct
    public void init() {
    }

    public <T> List<T> get(String type) {
        return Info.getInstance().refDataGet(type);
    }

    public void setPrivileges() {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        privilegios = new HashMap<String, HashMap<Integer, List<String>>>();
        List<String> moduleTempList;
        try {
            StringBuilder query = new StringBuilder(" SELECT PROFILES.USERNAME, PRIVILEGE.PRIVILEGEID, MODULEVERSION.CODE FROM PROFILEPRIVILEGE A ");
            query.append(" INNER JOIN PROFILES ON A.ID_PROFILE = PROFILES.ID_PROFILE ");
            query.append(" INNER JOIN CONF_PRIVILEGES PRIVILEGE ON A.PRIVILEGEID = PRIVILEGE.PRIVILEGEID ");
            query.append(" INNER JOIN (SELECT moduleid, code FROM MODULEVERSION group by moduleid, code) MODULEVERSION ON A.MODULEID = MODULEVERSION.MODULEID ");
            query.append(" WHERE A.TODATE IS NULL OR A.TODATE >= CURRENT_DATE ");

            List<Object[]> privilegesList = jpa.getNativeResultList(query.toString());

            if (privilegesList != null && !privilegesList.isEmpty()) {
                HashMap<Integer, List<String>> privilegeModuleMap;
                for (Object[] privilegeObject : privilegesList) {
                    privilegeModuleMap = new HashMap<Integer, List<String>>();
                    moduleTempList = new ArrayList<>();

                    String user = privilegeObject[0].toString().toUpperCase();
                    int idPriv = Integer.valueOf(privilegeObject[1].toString());
                    String module = privilegeObject[2].toString();

                    if (privilegios.containsKey(user)) {
                        if (privilegios.get(user).containsKey(idPriv)) {
                            privilegios.get(user).get(idPriv).add(module);
                        } else {
                            moduleTempList.add(module);
                            privilegios.get(user).put(idPriv, moduleTempList);
                        }
                    } else {
                        moduleTempList.add(module);
                        privilegeModuleMap.put(idPriv, moduleTempList);
                        privilegios.put(user, privilegeModuleMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jpa.close();
        }
    }

    public void setBooleansPrivegios(){
        boolean imp = false;
        boolean gen = false;
        boolean val = false;
        boolean view = false;
        
        setImportBoolean( imp || view);
        setValidate(val || view);
        setGenerate(gen || view); 
        setView(view);
        setImportBooleanOnly(imp);
        setValidateOnly(val);
        setGenerateOnly(gen);
    }
    
    public HashMap<String, HashMap<Integer, List<String>>> getPrivilegios() {
        return privilegios;
    }

    public void setPrivilegios(HashMap<String, HashMap<Integer, List<String>>> privilegios) {
        this.privilegios = privilegios;
    }


// Reporting BdP
    

    private enum WebPage {
    }

    public Map<String, Integer> getMonthsMap() {
        return monthsMap;
    }

    public void setMonthsMap(Map<String, Integer> monthsMap) {
        this.monthsMap = monthsMap;
    }

    public void loadRefData(Boolean isReaload) {
        if(isReaload){
        }
        Info.getInstance().loadRefData(isReaload);
    }

    public Integer getImportID() {
        return Constants.IMPORTID;
    }

    public Integer getGenerateID() {
        return Constants.GENERATEID;
    }

    public Integer getValidateID() {
        return Constants.VALIDATEID;
    }

    public Integer getViewID() {
        return Constants.VIEWID;
    }

    public Integer getLockID() {
        return Constants.LOCKID;
    }

    public Integer getUnLockID() {
        return Constants.UNLOCKID;
    }
    
    public String getEmptyMessage() {
        return Constants.emptyMessage;
    }

    public boolean isImportBoolean() {
        return importBoolean;
    }

    public void setImportBoolean(boolean importBoolean) {
        this.importBoolean = importBoolean;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

    public boolean isImportBooleanOnly() {
        return importBooleanOnly;
    }

    public void setImportBooleanOnly(boolean importBooleanOnly) {
        this.importBooleanOnly = importBooleanOnly;
    }

    public boolean isValidateOnly() {
        return validateOnly;
    }

    public void setValidateOnly(boolean validateOnly) {
        this.validateOnly = validateOnly;
    }

    public boolean isGenerateOnly() {
        return generateOnly;
    }

    public void setGenerateOnly(boolean generateOnly) {
        this.generateOnly = generateOnly;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public boolean isIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    
}