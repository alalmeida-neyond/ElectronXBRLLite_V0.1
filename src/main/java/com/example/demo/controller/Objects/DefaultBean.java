package com.example.demo.controller.Objects;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.el.ELException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.FacesException;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import org.jboss.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.example.demo.controller.Objects.Entities.InFileHistory;
import com.example.demo.controller.Objects.Entities.ModuleVersion;



public abstract class DefaultBean {

    private final Logger LOG = Logger.getLogger(DefaultBean.class);

    //@ManagedProperty(value = "#{refData}")
    @Autowired
    @Qualifier("refData")
    protected RefDataBean refData;

    public RefDataBean getRefData() {
        return refData;
    }

    public void setRefData(RefDataBean refData) {
        this.refData = refData;
    }


    private ModuleVersion moduleVersion;
    private ConfEntities entity;
    private LocalDate referenceDate;

    private Integer year;
    private Integer month;
    private String domain;
    private String version;
    private String statusMessage;

    private ModuleVersion moduleVersionExecution;
    private ConfEntities entityExecution;
    private Integer yearExecution;
    private Integer monthExecution;
    private String domainExecution;

    private List<InFileHistory> resultList;
    private List<ModuleVersion> modulesValidation;
    private String reportDate;


    public List<ModuleVersion> getModulesByDate(Integer year, Integer month, Integer privilege, boolean withView) {
        List<ModuleVersion> modulesResults = new ArrayList<>();
        List<ModuleVersion> filteredModules = new ArrayList<>();
        if ((year != null && !"".equals(year)) && (month != null && !"".equals(month))) {
            LocalDate dateFiltered = getDateAtLastDay(month, year);

            try {
                modulesResults = new ArrayList<ModuleVersion>(Info.getInstance().refDataGet(Constants.ModuleVersionAll));
                filteredModules = modulesResults.stream().filter(mv -> mv.getFromReferenceDate().compareTo(dateFiltered) <= 0
                        && mv.getToReferenceDate().compareTo(dateFiltered) >= 0 && mv.getFromReferenceDate().compareTo(mv.getToReferenceDate()) != 0
                ).collect(Collectors.toList());
                filteredModules.sort(Comparator.comparing(ModuleVersion::getCode));
            } catch (Exception e) {
            }
        }
        return filteredModules;
    }


    /**
     * Get the formatted date (YYYYMMDD), with the last day of month
     *
     * @param month
     * @param year
     * @return
     */
    public String getDateAtLastDayFormatted(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());
        return date.format(Constants.dateFormat);
    }

    /**
     * Get LocalDate from year and month at last day of that month
     *
     * @param month
     * @param year
     * @return
     */
    public LocalDate getDateAtLastDay(int month, int year) {
        LocalDate date = LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());
        return date;
    }

    @SuppressWarnings("null")
    public void downloadDashboard(String moduleID, String entityID, String ano, String mes) throws FileNotFoundException, IOException {
        if (moduleID == null) {
            moduleID = "";
        }
        if (entityID == null) {
            entityID = "";
        }
        if (ano == null) {
            ano = "";
        }
        if (mes == null) {
            mes = "";
        }

        if (!moduleID.equals("") && !entityID.equals("") && !ano.equals("") && !mes.equals("")) {

            List<Object[]> listOfDashboard;
            String filename = "DashboardValidacoes.xlsx";
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet spreadsheet = workbook.createSheet("Dashboard de Validações");
            XSSFRow row;


            listOfDashboard = null;//entityManager.createNativeQuery("SELECT module, refdata, sociedade ||'-'|| n.description sociedade,processdate, validation_ok,validation_dnrr, validation_warning, validation_error, validation_eba, validation_, validation_egdq, validation_ecb, validation_srb, validation_bst, validation_total,completed_import FROM CF_ED.EDT015_VALIDATION_REPORT m left join cf_mc.mct005_entity_scope_xbrl n on m.entityID = n.sociedade_id WHERE REFDATA = " + refData + " AND ENTITYID = " + entityID + " order by processdate desc").getResultList();


            if (!listOfDashboard.isEmpty()) {
                for (int i = 0; i < listOfDashboard.size(); i++) {
                    if (listOfDashboard.get(i)[15].toString().equals("0")) {
                        listOfDashboard.get(i)[15] = "Não";
                    } else {
                        listOfDashboard.get(i)[15] = "Sim";
                    }
                }
            }

            int rowid = 2;
            for (Object[] LoM : listOfDashboard) {
                row = spreadsheet.createRow(rowid++);
                int cellid = 0;
                for (Object obj : LoM) {
                    Cell cell = row.createCell(cellid++);
                    if (obj instanceof BigDecimal) {
                        obj = String.valueOf(obj);
                    }
                    cell.setCellValue((String) obj);
                }
            }

            ArrayList<String[]> listOfHeader = new ArrayList<String[]>();
            String[] header1 = new String[]{"", "", "", "", "Resultados", "", "", "", "Tipologia", "", "", "", "", "", "", ""};
            String[] header2 = new String[]{"Módulo", "Data Ref.", "Entidade", "Data Process.", "Ok", "DNRR", "Warnings", "Errors", "EBA", "", "EGDQ", "ECB", "SRB", "BST", "Total", "Relatórios Cruzados Importados?"};
            listOfHeader.add(header1);
            listOfHeader.add(header2);

            int rowidHeader = 0;
            for (String[] LoM : listOfHeader) {
                row = spreadsheet.createRow(rowidHeader++);
                int cellid = 0;
                for (Object obj : LoM) {
                    Cell cell = row.createCell(cellid++);
                    if (obj instanceof BigDecimal) {
                        obj = String.valueOf(obj);
                    }
                    cell.setCellValue((String) obj);
                }
            }

            FacesContext facesContext = FacesContext.getCurrentInstance();

            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            response.setContentType("application/vnd.ms-excel");

            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            try {

                ServletOutputStream responseOutputStream = response.getOutputStream();

                workbook.write(responseOutputStream);

                responseOutputStream.flush();

                responseOutputStream.close();

                facesContext.responseComplete();

                System.out.println("Download do ficheiro " + filename);

            } catch (ELException | IOException | FacesException e) {

                e.printStackTrace();

                FacesMessage msg = new FacesMessage("Aviso", "Erro ao gerar ficheiro");

                msg.setSeverity(FacesMessage.SEVERITY_WARN);

                FacesContext.getCurrentInstance().addMessage(null, msg);

            }
            workbook.close();
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erro: ", "Filtros não preenchidos!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public static String paddEntity(Integer entityId) {
        return Utils.pad4(entityId);
    }

    public List<InFileHistory> getResultList() {
        return resultList;
    }

    public void setResultList(List<InFileHistory> resultList) {
        this.resultList = resultList;
    }
    
    public List<ConfEntities> getVersions() {
        return Info.getInstance().refDataGet(Constants.VersionsAll);
    }

    public List<Integer> getYears() {
        return Utils.getYears();
    }

    public List<Object[]> getMonths() {
        return Utils.getMonths();
    }

    public List<ConfEntities> getEntities() {
        return Info.getInstance().refDataGet(Constants.EntitiesAll);
    }

    public List<ModuleVersion> getDomains() {
        return Info.getInstance().refDataGet(Constants.DomainsAll);
    }

    public List<ModuleVersion> getAllModules() {
        return Info.getInstance().refDataGet(Constants.ModuleVersionAll);
    }

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public ConfEntities getEntity() {
        return entity;
    }

    public void setEntity(ConfEntities entity) {
        this.entity = entity;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<ModuleVersion> getModulesValidation() {
        return modulesValidation;
    }

    public void setModulesValidation(List<ModuleVersion> modulesValidation) {
        this.modulesValidation = modulesValidation;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public ModuleVersion getModuleVersionExecution() {
        return moduleVersionExecution;
    }

    public void setModuleVersionExecution(ModuleVersion moduleVersionExecution) {
        this.moduleVersionExecution = moduleVersionExecution;
    }

    public ConfEntities getEntityExecution() {
        return entityExecution;
    }

    public void setEntityExecution(ConfEntities entityExecution) {
        this.entityExecution = entityExecution;
    }

    public Integer getYearExecution() {
        return yearExecution;
    }

    public void setYearExecution(Integer yearExecution) {
        this.yearExecution = yearExecution;
    }

    public Integer getMonthExecution() {
        return monthExecution;
    }

    public void setMonthExecution(Integer monthExecution) {
        this.monthExecution = monthExecution;
    }

    public String getDomainExecution() {
        return domainExecution;
    }

    public void setDomainExecution(String domainExecution) {
        this.domainExecution = domainExecution;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public ModuleVersion getModuleByFilenameInfo(LocalDate referenceDate, String moduleCode) {
        List<ModuleVersion> moduleVersionList = new ArrayList<>();
        if (moduleCode.isEmpty() || moduleCode == null) {
            return null;
        }
        try {
            for (ModuleVersion mv : new ArrayList<ModuleVersion>(Info.getInstance().refDataGet(Constants.ModuleVersionAll))) {
                if (mv.getCode().replace("_", "").trim().toLowerCase().equals(moduleCode.toLowerCase())
                        && mv.getFromReferenceDate().compareTo(referenceDate) <= 0 && mv.getToReferenceDate().compareTo(referenceDate) >= 0
                        && mv.getFromReferenceDate().compareTo(mv.getToReferenceDate()) != 0) {
                    moduleVersionList.add(mv);
                }
            }

//            moduleVersionList = new ArrayList<ModuleVersion>(Info.getInstance().refDataGet(Constants.ModuleVersionAll)).stream().filter(mv ->
//                mv.getCode().trim().toLowerCase().equals(moduleCode.toLowerCase()) &&
//                mv.getFromReferenceDate().compareTo(referenceDate) <= 0 && mv.getToReferenceDate().compareTo(referenceDate) >= 0).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return !moduleVersionList.isEmpty() ? moduleVersionList.get(0) : null;
    }

    public ConfEntities getEntityByFilenameInfo(String entityBST) {
        if (entityBST == null) {
            LOG.info("entityBST:" + entityBST);
            return null;
        }
        LOG.info("EntityBST:" + entityBST);
        List<ConfEntities> entitiesList = new ArrayList<ConfEntities>(Info.getInstance().refDataGet(Constants.EntitiesAll)).stream().filter(ent
                -> ent.getBdpId().equals(entityBST)).collect(Collectors.toList());

        return !entitiesList.isEmpty() ? entitiesList.get(0) : null;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(Integer year, Integer month) {
        if (year != null && month != null) {
            this.referenceDate = getDateAtLastDay(month, year);
        } else {
            this.referenceDate = null;
        }
    }

    public void throwFacesMessage(Severity severity, String summary, String detail) {
        FacesMessage msg = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void changeMonthAndModule(){
        setMonth(null);
        setModuleVersion(null);
    }

}
