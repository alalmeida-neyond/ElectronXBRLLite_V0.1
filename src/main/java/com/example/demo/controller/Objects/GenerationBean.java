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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;

import com.example.demo.controller.Objects.DAL.*;
import com.example.demo.controller.Objects.Entities.*;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.jboss.logging.Logger;

@Named(value = "generationBean")
@ViewScoped
public class GenerationBean extends DefaultBean {

    private final Logger LOG = Logger.getLogger(GenerationBean.class.getName());
    // private List<IO> generationIOs;

    // Log attributes
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

        // this.lazy = IODAL.getIOsByAction(mv == null ? null : mv.getModuleVID(),
        // referenceDate, ent == null ? null : ent.getEntityID(), domain,
        // Constants.actionGeneration, Constants.GENERATEID,
        // session.getUser().getUserId(),
        // session.getUser().getProfile().equals(Constants.Admin) ? true : false);
        generationIOs = IODAL.getIOsByAction(mv == null ? null : mv.getModuleVID(), referenceDate,
                ent == null ? null : ent.getEntityID(), domain, Constants.actionGeneration, Constants.GENERATEID);
    }

    public static File getLastModified(String directoryFilePath) {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        if (files != null) {
            for (File file : files) {
                if (file.lastModified() > lastModifiedTime) {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return chosenFile;
    }

    public void startGeneration(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename) {
        List<IO> operationsRunningFromIO = IODAL.getOperationRunningFromIO(moduleVersion, domain, entity, referenceDate.toString());
        if (!operationsRunningFromIO.isEmpty()) {
            LOG.info(Constants.concurrentOperations + Constants.concurrentOperationsDesc);
            return;
        }

        String threadName = Constants.GEN + "_" + moduleVersion.getCode() + "_" + domain + "_"
                + referenceDate.toString() + "_" + entity.getBdpId();
        try {
            XBRLGenerationController generationController = new XBRLGenerationController(threadName, moduleVersion, domain, entity, referenceDate);
            generationController.xbrlGenerationMain();
        } catch (Exception e) {
            LOG.warn("Geração | Erro na pesquisa logs geração", e);
            return;
        }
        LOG.info(Constants.generationStarted + Constants.generationStartedDesc);

    }

    public boolean isGenerationRunning() {
        return generationRunning;
    }

    public void setGenerationRunning(boolean generationRunning) {
        this.generationRunning = generationRunning;
    }

}
