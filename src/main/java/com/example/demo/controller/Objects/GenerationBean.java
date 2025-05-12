/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.example.demo.controller.Objects.DAL.*;
import com.example.demo.controller.Objects.Entities.*;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.util.logging.Logger;

@Named(value = "importFileBean")
@ViewScoped
public class GenerationBean extends DefaultBean {

    private final Logger LOG = Logger.getLogger(GenerationBean.class.getName());
    //private List<IO> generationIOs;

    //Log attributes
    private boolean generationRunning;

    private List<IO> generationIOs;


    @PostConstruct
    public void init() {
        getGenerationIOs(refData.getGenerateID(), false, false);
    }

    public void getGenerationIOs(Integer privilege, boolean withView, boolean triggeredFromUser) {
        LocalDate referenceDate = null;
        ModuleVersion mv = getModuleVersion();
        ConfEntities ent = getEntity();
        String domain = getDomain();

        if (getYear() != null && getMonth() != null) {
            referenceDate = getDateAtLastDay(getMonth(), getYear());
        }
        
        //this.lazy = IODAL.getIOsByAction(mv == null ? null : mv.getModuleVID(), referenceDate, ent == null ? null : ent.getEntityID(), domain, Constants.actionGeneration, Constants.GENERATEID, session.getUser().getUserId(), session.getUser().getProfile().equals(Constants.Admin) ? true : false);
        generationIOs= IODAL.getIOsByAction(mv == null ? null : mv.getModuleVID(), referenceDate, ent == null ? null : ent.getEntityID(), domain, Constants.actionGeneration, Constants.GENERATEID);
    }

    public void startGeneration() {
        String domain = "";
        if (getModuleVersionExecution() == null || getEntityExecution() == null || getYearExecution() == null || getYearExecution() == null || getMonthExecution() == null || getMonthExecution() == null || getDomainExecution() == null || getDomainExecution() == null) {
            throwFacesMessage(FacesMessage.SEVERITY_WARN, Constants.generationError, Constants.missingFilters);
            return;
        } else {
            LocalDate referenceDate = getDateAtLastDay( getMonthExecution(),getYearExecution());

            domain = getDomainExecution().length() > Constants.DOMAINLENGTH ? getDomainExecution().substring(0, 3).toUpperCase() : getDomainExecution().toUpperCase();
            List<IO> operationsRunningFromIO = IODAL.getOperationRunningFromIO(getModuleVersionExecution(), domain, getEntityExecution(), referenceDate.toString());
            if (!operationsRunningFromIO.isEmpty()) {
                throwFacesMessage(FacesMessage.SEVERITY_INFO, Constants.concurrentOperations, Constants.concurrentOperationsDesc);
                return;
            }
                
            String threadName = Constants.GEN + "_" + getModuleVersionExecution().getCode() + "_" + domain + "_" + referenceDate.toString() + "_" + getEntityExecution().getBdpId();
            try {
                //Thread t = new Thread(new XBRLGenerationController(session.getUser(), threadName, getModuleVersionExecution(), domain, getEntityExecution(), referenceDate));
                Thread t = new Thread(new XBRLGenerationController(threadName, getModuleVersionExecution(), domain, getEntityExecution(), referenceDate));
                t.setName(threadName);
                t.start();
            } catch (Exception e) {
                LOG.log(Level.SEVERE,"Geração | Erro na pesquisa logs geração", e);
                return;
            }
            throwFacesMessage(FacesMessage.SEVERITY_INFO, Constants.generationStarted, Constants.generationStartedDesc);
        }
    }

    public boolean isGenerationRunning() {
        return generationRunning;
    }

    public void setGenerationRunning(boolean generationRunning) {
        this.generationRunning = generationRunning;
    }

}
