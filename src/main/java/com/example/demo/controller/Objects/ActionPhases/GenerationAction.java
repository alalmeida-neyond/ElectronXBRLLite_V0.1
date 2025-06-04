package com.example.demo.controller.Objects.ActionPhases;

import java.time.LocalDate;
import java.util.List;
import java.io.File;
import com.example.demo.controller.Objects.*;
import com.example.demo.controller.Objects.Conf.ConfEntities;
import com.example.demo.controller.Objects.DAL.*;
import com.example.demo.controller.Objects.Entities.*;

import org.jboss.logging.Logger;

public class GenerationAction {

    private final Logger LOG = Logger.getLogger(GenerationAction.class.getName());

    private boolean generationRunning;

    private List<IO> generationIOs;

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

    public void startGeneration(LocalDate referenceDate, ModuleVersion moduleVersion, String domain, ConfEntities entity, String filename, IO ioImport, IO ioValidation) {
        List<IO> operationsRunningFromIO = IODAL.getOperationRunningFromIO(moduleVersion, domain, entity, referenceDate.toString());
        if (!operationsRunningFromIO.isEmpty()) {
            LOG.info(Constants.concurrentOperations + Constants.concurrentOperationsDesc);
            return;
        }

        String threadName = Constants.GEN + "_" + moduleVersion.getCode() + "_" + domain + "_"
                + referenceDate.toString() + "_" + entity.getBdpId();
        try {
            XBRLGenerationController generationController = new XBRLGenerationController(threadName, moduleVersion, domain, entity, referenceDate);
            generationController.xbrlGenerationMain(referenceDate,moduleVersion,domain,entity,ioImport, ioValidation);
        } catch (Exception e) {
            LOG.warn("Geracao | Erro na pesquisa logs geracao", e);
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
