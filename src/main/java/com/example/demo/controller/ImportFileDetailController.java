/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.jboss.logging.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTOs.ImportedDetailsDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Beans.DefaultBean;
import com.example.demo.controller.Objects.Entities.Conf.ConfEntities;
import com.example.demo.controller.Objects.Entities.DAL.IODAL;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.IO.IO;

@RestController
@RequestMapping("/importFileDetail")
public class ImportFileDetailController extends DefaultBean {

    private final Logger LOG = Logger.getLogger(ImportFileDetailController.class);

    private IO ioImport;
    private List<ImportedDetailsDTO> impDetails;
    private LocalDate referenceDate = null;

    @PostConstruct
    public void init() {
        String ioid = "";
        if (ioid != null) {
            setIoImport(IODAL.getIOById(ioid));
        }

        if (ioImport != null) {
            setDomain(getIoImport().getDomain());
            setEntity(getIoImport().getEntity());
            setModuleVersion(getIoImport().getModule());
            this.referenceDate = getDateAtLastDay(getIoImport().getReferenceDate().getMonthValue(), getIoImport().getReferenceDate().getYear());

            getImportDetails(refData.getImportID());
        }
    }

    public List<ImportedDetailsDTO> getImportDetails(Integer privilege) {
        List<Object[]> tempReports = new ArrayList<>();
        if (ioImport == null) {
        //if (!triggeredByUser ) {
            return impDetails;
        }
        impDetails = new ArrayList<ImportedDetailsDTO>();
        if (getYear() != null && getMonth() != null) {
            //CastMonth into number
            this.referenceDate = getDateAtLastDay(getMonth(), getYear());
        } else {
            if (ioImport == null) {
            //if (triggeredByUser) {
                LOG.info("Detalhes importacao:" + Constants.missingRefDate);
                return impDetails;
            }
        }
        String domain = getDomain() != null ? getDomain().length() > Constants.DOMAINLENGTH ? getDomain().substring(0, 3).toUpperCase() : getDomain().toUpperCase() : null;
        
        tempReports = getImportDetails(getModuleVersion(), referenceDate.format(Constants.DATEFORMATISO8601), getEntity(), domain, getIoImport());

        if (tempReports.isEmpty() || tempReports == null) {
            LOG.info("Detalhes importacao:" + Constants.emptyMessage);
            return impDetails;
        }

        LOG.info("TempReports" + tempReports.size());
        
        for (Object obj[] : tempReports) {
            ImportedDetailsDTO impDetail = new ImportedDetailsDTO(
                obj[0] != null ? obj[0].toString() : null,
                obj[1] != null ? obj[1].toString() : null,
                obj[2] != null ? obj[2].toString() : null,
                obj[3] != null ? obj[3].toString() : null,
                obj[4] != null ? obj[4].toString() : null,
                obj[5] != null ? obj[5].toString() : null
            );            impDetails.add(impDetail);
        }
        return impDetails;
    }

    public IO getIoImport() {
        return ioImport;
    }

    public void setIoImport(IO ioImport) {
        this.ioImport = ioImport;
    }

    public List<ImportedDetailsDTO> getImpDetails() {
        return impDetails;
    }

    public void setImpDetails(List<ImportedDetailsDTO> impDetails) {
        this.impDetails = impDetails;
    }

    private List<Object[]> getImportDetails(ModuleVersion module, String referenceDate, ConfEntities entity, String domain, IO io) {
        JPA<Object[]> jpa = new JPA<Object[]>(Object[].class);
        List<Object[]> tempReports = new ArrayList<>();
        
        try {
            /*tempReports = jpa.getNativeResultList(Utils.getResource("GetImportedDetails.sql"),
                    "actionImportId", String.valueOf(Constants.actionImport),
                    "referenceDate", referenceDate,
                    "domain", domain,
                    //"entityID", entity == null ? null : entity.getEntityID(),
                    "moduleVID", module == null ? null : module.getModuleVID(),
                    "ioid", ioImport == null ? null : ioImport.getIoId());*/

            //Com a pesquisa de IO
            /*tempReports = jpa.getNativeResultList(Utils.getResource("GetImportedDetails.sql"),
                    "actionImportId", String.valueOf(Constants.actionImport),
                    "referenceDate", referenceDate,
                    "format",Constants.ISOBASEFORMAT8601SQLite,
                    "domain", domain,
                    "entityID", entity == null ? null : entity.getEntityID(),
                    "moduleVID", module == null ? null : module.getModuleVID());*/

            //Sem a pesquisa IO
            tempReports = jpa.getNativeResultList(Utils.getResource("GetImportedDetails.sql"),
                    "format",Constants.ISOBASEFORMAT8601SQLite,
                    "ioId", io.getIoId());
        } catch (Exception e) {
            LOG.error("Detalhes importacao | Erro no processo de obtencao da query GetImportedDetails.sql. ", e);
        }
        
        return tempReports;
    }

}
