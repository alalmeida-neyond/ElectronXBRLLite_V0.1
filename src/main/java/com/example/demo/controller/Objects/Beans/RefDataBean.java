/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
    //private ActiveUser user = new ActiveUser();
    
    public RefDataBean() {

        // Reporting XML
        webPageMap.put("xbrl2_header1|Reporting Bdp 2.0", HEADER1_REPORTING_BDP);

        webPageMap.put("xbrl2_header2|Templates", HEADER2_BdP_TEMPLATES);
        webPageMap.put("xbrl2_header2|Importacao", HEADER2_BdP_IMPORTACAO_RELATORIOS);
        webPageMap.put("xbrl2_header2|Validacao", HEADER2_BdP_VALIDACOES);
        webPageMap.put("xbrl2_header2|Geracao", HEADER2_BdP_GERACAO_XBRL);
        webPageMap.put("xbrl2_header2|Dashboard Geral", HEADER2_BdP_DASHBOARD_GERAL);
        webPageMap.put("xbrl2_header2|Parametrizacoes", HEADER2_BdP_PARAMETRIZACOES);
    }

    @PostConstruct
    public void init() {
        //setPrivileges();
        //user.init();    
        //setBooleansPrivegios();    
        loadRefData(false);
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
        Map<String, HashMap<Integer, List<String>>> privilegesAll = getPrivilegios();
        /*if(privilegesAll.containsKey(user.getUserId().toUpperCase())){
            Map<Integer, List<String>> privilegesUser = privilegesAll.get(user.getUserId().toUpperCase());
            imp = privilegesUser.containsKey(Constants.IMPORTID);
            gen  = privilegesUser.containsKey(Constants.GENERATEID);
            val  = privilegesUser.containsKey(Constants.VALIDATEID);
            view  = privilegesUser.containsKey(Constants.VIEWID);
        }*/
        setImportBoolean( imp || view);
        setValidate(val || view);
        setGenerate(gen || view); 
        setView(view);
        setImportBooleanOnly(imp);
        setValidateOnly(val);
        setGenerateOnly(gen);
        //setIsAdmin(user.getProfile().equals(Constants.Admin));
    }
    
    public HashMap<String, HashMap<Integer, List<String>>> getPrivilegios() {
        return privilegios;
    }

    public void setPrivilegios(HashMap<String, HashMap<Integer, List<String>>> privilegios) {
        this.privilegios = privilegios;
    }

    public boolean canSelectButton(String webpageName, String menuName, String tabName) {
        webpageName = webpageName.trim();

        WebPage[] webPages = webPageMap.get(menuName + "|" + tabName);

        if (webPages != null) {
            for (WebPage webPage : webPages) {
                if (webPage.getName().equalsIgnoreCase(webpageName)) {
                    return true;
                }
            }
        }

        return false;
    }

// Reporting BdP
    //TODO complete lists
    private final static WebPage[] HEADER1_REPORTING_BDP = new WebPage[]{WebPage.bpd_templates, WebPage.import_file, WebPage.imported_files_list, WebPage.imported_file_maps_list, WebPage.imported_file_cells_list, WebPage.imported_files_export, WebPage.imported_files_resume, WebPage.imported_rules_list, WebPage.create_rules};
//TODO
//    private final static WebPage[] HEADER2_CONF_LOGS = new WebPage[]{WebPage.conf_logs};
//    private final static WebPage[] HEADER2_CONF = new WebPage[]{WebPage.config, WebPage.config_datapoints, WebPage.config_directories, WebPage.config_managment, WebPage.config_regras, WebPage.config_templates, WebPage.config_validations, WebPage.config_xbrl, WebPage.margem_erro, WebPage.DPM_rulesView, WebPage.manual_rulesView, WebPage.config_regulator_rules};

    private final static WebPage[] HEADER2_BdP_IMPORTACAO_RELATORIOS = new WebPage[]{WebPage.import_file, WebPage.imported_files_list, WebPage.imported_file_maps_list, WebPage.imported_file_cells_list, WebPage.imported_files_export, WebPage.imported_files_resume, WebPage.imported_rules_list, WebPage.create_rules, WebPage.import_details};
    private final static WebPage[] HEADER2_BdP_VALIDACOES = new WebPage[]{WebPage.execute_validation, WebPage.results_validation, WebPage.rules_validation, WebPage.resume_validation, WebPage.dashboard_validation, WebPage.validations_details, WebPage.validations_results_details};
    private final static WebPage[] HEADER2_BdP_GERACAO_XBRL = new WebPage[]{WebPage.execute_generation, WebPage.download_generation};
    private final static WebPage[] HEADER2_BdP_DASHBOARD_GERAL = new WebPage[]{WebPage.dashboard_geral};
    private final static WebPage[] HEADER2_BdP_PARAMETRIZACOES = new WebPage[]{WebPage.paramsMandatoryReports};
    private final static WebPage[] HEADER2_BdP_TEMPLATES = new WebPage[]{WebPage.bpd_templates};

    private enum WebPage {
        // Reporting BdP 2.0
        bpd_templates(100000, "/XBRL_2.0/download_template.xhtml"),
        index_BdP(100001, "/XBRL_2.0/index_xbrl2.xhtml"),
        import_file(100002, "/XBRL_2.0/import_file.xhtml"),
        imported_files_list(100003, "/XBRL_2.0/imported_files_list.xhtml"),
        imported_file_maps_list(100004, "/XBRL_2.0/imported_file_maps_list.xhtml"),
        imported_file_cells_list(100005, "/XBRL_2.0/imported_file_cells_list.xhtml"),
        imported_files_export(100006, "/XBRL_2.0/imported_files_export.xhtml"),
        imported_files_resume(100007, "/XBRL_2.0/imported_files_resume.xhtml"),
        imported_rules_list(100008, "/XBRL_2.0/imported_rules_list.xhtml"),
        create_rules(100009, "/XBRL_2.0/create_rules.xhtml"),
        execute_validation(100010, "/XBRL_2.0/run_Validations.xhtml"),
        results_validation(100011, "/XBRL_2.0/validations_results.xhtml"),
        rules_validation(100012, "/XBRL_2.0/view_validations_rules.xhtml"),
        resume_validation(100013, "/XBRL_2.0/validations_resume.xhtml"),
        dashboard_validation(100014, "/XBRL_2.0/validations_dashboard.xhtml"),
        execute_generation(100015, "/XBRL_2.0/generate_xbrl.xhtml"),
        download_generation(100016, "/XBRL_2.0/generateXBRL_files.xhtml"),
        dashboard_geral(100017, "/XBRL_2.0/dashboard_geral_DPMv2.xhtml"),
        paramsMandatoryReports(100018, "/XBRL_2.0/mandatoryReportsParameterization.xhtml"),
        validations_details(100019, "/XBRL_2.0/validations_detail.xhtml"),
        validations_results_details(100020, "/XBRL_2.0/validations_results_details.xhtml"),
        import_details(100021, "/XBRL_2.0/import_file_detail.xhtml");

        private String name;
        private int id;

        private WebPage(int id, String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

    }

    public Map<String, Integer> getMonthsMap() {
        return monthsMap;
    }

    public void setMonthsMap(Map<String, Integer> monthsMap) {
        this.monthsMap = monthsMap;
    }

    public void loadRefData(Boolean isReaload) {
        if(isReaload){
            //setPrivileges();
            //setBooleansPrivegios();
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

    /*public ActiveUser getUser() {
        return user;
    }

    public void setUser(ActiveUser user) {
        this.user = user;
    }*/
    
}
