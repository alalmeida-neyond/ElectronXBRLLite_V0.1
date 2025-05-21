package com.example.demo.controller.Objects;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;

import org.springframework.stereotype.Component;

import jakarta.persistence.*;

import jakarta.enterprise.context.SessionScoped;




@Component(value = "lockBean")
@SessionScoped
@ViewScoped
public class LockBean implements Serializable {

    private String lockImport;
    private String lockValidation;
    private String lockXBRL;
    private String lockDPM;
    private String importStatus="images/IconGreenSmall.gif";
    private String importStatusMessage= "Processo Terminado/Desbloqueado, clique nos botões acima para iniciar a operacao.";
    private String validationStatus;
    private String validationStatusMessage;
    private String xbrlStatus;
    private String xbrlStatusMessage;
    private Boolean lockXBRLBtn = false;
    private Boolean lockImportBtn = false;
    private Boolean lockValidationBtn = false;
    private Boolean unlockXBRLBtn = false;
    private Boolean unlockImportBtn = false;
    private Boolean unlockValidationBtn = false;
    private Boolean lockDpmBtn = false;
    private Boolean unlockDpmBtn = false;
    private Boolean running = false;

    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public Boolean getLockDpmBtn() {
        return lockDpmBtn;
    }

    public void setLockDpmBtn(Boolean lockDpmBtn) {
        this.lockDpmBtn = lockDpmBtn;
    }

    public Boolean getUnlockDpmBtn() {
        return unlockDpmBtn;
    }

    public void setUnlockDpmBtn(Boolean unlockDpmBtn) {
        this.unlockDpmBtn = unlockDpmBtn;
    }

    public String getLockDPM() {
        return lockDPM;
    }

    public void setLockDPM(String lockDPM) {
        this.lockDPM = lockDPM;
    }

    public Boolean getLockXBRLBtn() {
        return lockXBRLBtn;
    }

    public void setLockXBRLBtn(Boolean lockXBRLBtn) {
        this.lockXBRLBtn = lockXBRLBtn;
    }

    public Boolean getLockImportBtn() {
        return lockImportBtn;
    }

    public void setLockImportBtn(Boolean lockImportBtn) {
        this.lockImportBtn = lockImportBtn;
    }

    public Boolean getLockValidationBtn() {
        return lockValidationBtn;
    }

    public void setLockValidationBtn(Boolean lockValidationBtn) {
        this.lockValidationBtn = lockValidationBtn;
    }

    public Boolean getUnlockXBRLBtn() {
        return unlockXBRLBtn;
    }

    public void setUnlockXBRLBtn(Boolean unlockXBRLBtn) {
        this.unlockXBRLBtn = unlockXBRLBtn;
    }

    public Boolean getUnlockImportBtn() {
        return unlockImportBtn;
    }

    public void setUnlockImportBtn(Boolean unlockImportBtn) {
        this.unlockImportBtn = unlockImportBtn;
    }

    public Boolean getUnlockValidationBtn() {
        return unlockValidationBtn;
    }

    public void setUnlockValidationBtn(Boolean unlockValidationBtn) {
        this.unlockValidationBtn = unlockValidationBtn;
    }

    private static final String PERSISTENCE_UNIT_NAME_ED = "UNMANAGEDPROCESS";
    private EntityManager em;

    public String getLockImport() {
        return lockImport;
    }

    public void setLockImport(String lockImport) {
        this.lockImport = lockImport;
    }

    public String getLockValidation() {
        return lockValidation;
    }

    public void setLockValidation(String lockValidation) {
        this.lockValidation = lockValidation;
    }

    public String getLockXBRL() {
        return lockXBRL;
    }

    public void setLockXBRL(String lockXBRL) {
        this.lockXBRL = lockXBRL;
    }

    public void init() {
        try {
            lockXBRLBtn = false;
            unlockXBRLBtn = false;
            //xbrlStatusMessage = "Selecione o módulo e data a gerar XBRL e clique no botão acima para iniciar a operacao.";
            //xbrlStatus = "images/IconGreenSmall.gif";
            lockXBRL = "Terminado/Desbloqueado";

            //initializeStatusEmpty();
            //initializeStatus();
            setValidationStatusMessage("Selecione o módulo, data e entidade a validar e clique no botão acima para iniciar a operacao.");
            //setValidationStatus("images/IconGreenSmall.gif");

        } catch (Exception ex) {
            System.out.println("LockBean cannot execute init(). Exception: " + ex.toString());
        }
    }

    public void lockOperation(String operation, String module) {
        try {
            System.out.println("Hello There2");
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME_ED);
            em = emf.createEntityManager();
            
            em.getTransaction().begin();
            em.getTransaction().commit();
            em.close();
            init();
            System.out.println("Application was locked for operation: " + operation + ".");
        } catch (Exception ex) {
            System.out.println("Application cannot lock operation: " + operation + ". Exception:" + ex.toString());
        }
    }

    public void unlockOperation(String operation, String module) {
        try {
            System.out.println("Hello There3");
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME_ED);
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.getTransaction().commit();

            em.close();
            init();
            System.out.println("Application was unlocked for operation: " + operation + ".");
        } catch (Exception ex) {
            System.out.println("Application cannot unlock operation: " + operation + ". Exception:" + ex.toString());
        }
    }

    public String getImportStatus() {
        return importStatus;
    }

    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }

    public String getImportStatusMessage() {
        return importStatusMessage;
    }

    public void setImportStatusMessage(String importStatusMessage) {
        this.importStatusMessage = importStatusMessage;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getValidationStatusMessage() {
        return validationStatusMessage;
    }

    public void setValidationStatusMessage(String validationStatusMessage) {
        this.validationStatusMessage = validationStatusMessage;
    }

    public String getXbrlStatus() {
        return xbrlStatus;
    }

    public void setXbrlStatus(String xbrlStatus) {
        this.xbrlStatus = xbrlStatus;
    }

    public String getXbrlStatusMessage() {
        return xbrlStatusMessage;
    }

    public void setXbrlStatusMessage(String xbrlStatusMessage) {
        this.xbrlStatusMessage = xbrlStatusMessage;
    }
    
    public boolean initializeStatusCMU(String module, String date, String operation, int entityID) {
        boolean running = false;

        System.out.println("Hello There4");
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME_ED);
        em = emf.createEntityManager();

        String queryStatus = "SELECT * FROM CF_ED.EDT997_APPSTATUS where APPMODULE = '" + module + "' and "
                + "APPREPORTDATE = '" + date + "' and APPSTATUS = 'RUNNING' and APPOPERATION = '" + operation + "' AND SOCIEDADE_ID= "+ entityID;
           

        List<Object[]> statusLst = em.createNativeQuery(queryStatus).getResultList();
        em.close();
        FacesMessage msg = null;
        if (statusLst.size() > 0) {
            if (operation.equals("ExportXBRL")) {
                String userOwner = getUserOwner(module, date, operation, entityID);
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Processo atualmente em execucao pelo utilizador: " + userOwner, "Módulo: " + module + " | Data: " + date);
                running = true;
            }
            if (operation.equals(ValidationTypeBean.ValidationType.ValidationFormulae.name()) || operation.equals(ValidationTypeBean.ValidationType.ValidationCNIICorp.name())) {
                String userOwner = getUserOwner(module, date, operation, entityID);
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Processo atualmente em execucao pelo utilizador: " + userOwner, "Módulo: " + module + " | Data: " + date);
                running = true;
            }
        } else {
            if (operation.equals("ExportXBRL")) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Processo de geracao de XBRL iniciado para:", "Módulo: " + module + " | Data: " + date);
                running = false;
            }
            if (operation.equals(ValidationTypeBean.ValidationType.ValidationFormulae.name()) || operation.equals(ValidationTypeBean.ValidationType.ValidationCNIILocal.name()) || operation.equals(ValidationTypeBean.ValidationType.ValidationCNIICorp.name())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Processo de validacao iniciado para:", "Módulo: " + module + " | Data: " + date);
                running = false;
            }
        }

        FacesContext.getCurrentInstance().addMessage(null, msg);
        return running;
    }

    public String getUserOwner(String module, String date, String operation, int entityID) {

        System.out.println("Hello There5");
        String userOwner = "";
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME_ED);
        em = emf.createEntityManager();
        try {
            String queryUserOwner = "SELECT APPUSER FROM CF_ED.EDT997_APPSTATUS where APPMODULE = '" + module + "' and "
                    + "APPREPORTDATE = '" + date + "' and APPSTATUS = 'RUNNING' and APPOPERATION = '" + operation + "' AND SOCIEDADE_ID= "+ entityID;

            Object statusUser = em.createNativeQuery(queryUserOwner).getSingleResult();

            userOwner = "" + statusUser.toString();
        } catch (NoResultException ex) {
            FacesMessage msg = null;
        
            if(operation.equals("ExportXBRL")){
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Processo Geracao de XBRL:", "O processo geracao de XBRL para o módulo: " + module + " Data: " + date + ", não se encontra em execucao!");
            }
            else if (operation.equals(ValidationTypeBean.ValidationType.ValidationFormulae.name()) || operation.equals(ValidationTypeBean.ValidationType.ValidationCNIICorp.name())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Processo ValidationFormulae:", "O processo validacao para o módulo: " + module + " Data: " + date + ", não se encontra em execucao!");
            }
            FacesContext.getCurrentInstance().addMessage(null, msg);
                
        } catch (Exception ex) {
            FacesMessage msg = null;
            if(operation.equals("ExportXBRL")){
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Processo Geracao de XBRL:", "O processo geracao de XBRL para o módulo: " + module + " Data: " + date + ", não se encontra em execucao!");
            }
            else if (operation.equals(ValidationTypeBean.ValidationType.ValidationFormulae.name()) || operation.equals(ValidationTypeBean.ValidationType.ValidationCNIICorp.name())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Processo ValidationFormulae:", "O processo validacao para o módulo: " + module + " Data: " + date + ", não se encontra em execucao!");
            }
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        //(Object[]) statusLstUser.get(0))[0];

        em.close();
        return userOwner;

    }

    public List<Object[]> moduleThreadRunning(List<String> modules, String operation) {

        System.out.println("Hello There");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME_ED);
        em = emf.createEntityManager();
        
        StringBuilder modulesIn = new StringBuilder();
        for(String module : modules) {
            modulesIn.append("'");
            modulesIn.append(module);
            modulesIn.append("',");
        }
        modulesIn.deleteCharAt(modulesIn.length()-1);
        
        String queryStatus = "SELECT * FROM Category";

        List<Object[]> resultsLst = em.createNativeQuery(queryStatus).getResultList();

        em.close();

        return resultsLst;
    }
    
    public Boolean getImportButtonState(String userID) {
        return getImportButtonState(userID, false);
    }
    
    public Boolean getImportButtonState(String userID, Boolean ES) {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        
        for(Thread thread : threadSet) {
            if(thread.getName().matches("\\s*tImport(NonStandard(FTP)?)?Excel_"+userID+"\\s*")) {
                if(!running) {
                    importStatusMessage = "Processo em Execucao/Bloqueado, por favor aguarde ou contacte o administrador.";
                    importStatus = "images/IconRed.gif";
                    running = true;
                }
                return true;
            }
        }
        if(running) {
            
            importStatusMessage = "Processo Terminado/Desbloqueado, clique nos botões acima para iniciar a operacao.";
            importStatus = "images/IconGreenSmall.gif";
            running = false;
        }
        return false;
    }
    
}
