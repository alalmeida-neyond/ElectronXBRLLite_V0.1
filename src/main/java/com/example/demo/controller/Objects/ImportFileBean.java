package com.example.demo.controller.Objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import org.jboss.logging.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;

import com.example.demo.controller.Objects.DAL.IODAL;
import com.example.demo.controller.Objects.DAL.OperationRunningDAL;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.service.ModuleFileImport;


@Named(value = "importFileBean")
@ViewScoped
public class ImportFileBean extends DefaultBean {

    private final Logger LOG = Logger.getLogger(ImportFileBean.class);

    private List<IO> importIOs;

    private String fileName;
    private Boolean isSubmitDisable = true;
    private Boolean isOperationOccuring = true;
    private List<Integer> listOfImportRulesToApply = new ArrayList<Integer>();
    private List<Integer> listOfImportRulesToAlwaysApply = new ArrayList<Integer>();
    private List<ConfImportRules> listOfImportRules;
    private MultipartFile file;

    /**
     * method called when the page to list the imported files open, making sure
     * the reported List is filled when the page loads
     */
    @PostConstruct
    public void init() {
        List<ConfImportRules> rules = Info.getInstance().refDataGet(Constants.ConfImportRulesAll);
        
        if (rules == null) {
            LOG.warn("No import rules found during init()");
            this.listOfImportRules = new ArrayList<>();
            this.listOfImportRulesToApply = new ArrayList<>();
            this.listOfImportRulesToAlwaysApply = new ArrayList<>();
            return;
        }

        setListOfImportRules(rules);
        listOfImportRulesToApply = rules.stream()
                .map(ConfImportRules::getImportRuleID)
                .collect(Collectors.toList());

        listOfImportRulesToAlwaysApply = rules.stream()
                .filter(ConfImportRules::isAlwaysRun)
                .map(ConfImportRules::getImportRuleID)
                .collect(Collectors.toList());

        getImportIOs(refData.getImportID(), true);
    }

    public void getImportIOs(Integer privilege, boolean withView) {        
        if (getYear() != null && getMonth() != null) {
            //CastMonth into number
            setReferenceDate(getYear(), getMonth());
        } else {
            setReferenceDate(null, null);
        }

        importIOs = IODAL.getIOsByAction(getModuleVersion() == null ? null : getModuleVersion().getModuleVID(), getReferenceDate() == null ? null : getReferenceDate(), getEntity() == null ? null : getEntity().getEntityID(), getDomain(), Constants.actionImport, Constants.VALIDATEID);

    }

    public void handleFileUpload(MultipartFile event) {
        FacesMessage message = new FacesMessage("Successful", event.getOriginalFilename() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void upload() {
        if (file != null) {
            if (onFileChange(validateFileName(getFile().getOriginalFilename()))) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Ficheiro pronto", "Ficheiro encontra-se pronto para ser importado");
                FacesContext.getCurrentInstance().addMessage(null, msg);

                try {
                    uploadFile(getFile());
                } catch (Exception e) {
                    LOG.error("Erro no upload do ficheiro para o servidor.", e);
                }
            }

        }
    }

    public List<ConfImportRules> getListOfImportRules() {
        return listOfImportRules;
    }

    public void setListOfImportRules(List<ConfImportRules> listOfImportRules) {
        this.listOfImportRules = listOfImportRules;
    }

    public List<Integer> getListOfImportRulesToAlwaysApply() {
        return listOfImportRulesToAlwaysApply;
    }

    public void setListOfImportRulesToAlwaysApply(List<Integer> listOfImportRulesToAlwaysApply) {
        this.listOfImportRulesToAlwaysApply = listOfImportRulesToAlwaysApply;
    }

    public List<Integer> getListOfImportRulesToApply() {
        return listOfImportRulesToApply;
    }

    public void setListOfImportRulesToApply(List<Integer> listOfImportRulesToApply) {
        this.listOfImportRulesToApply = listOfImportRulesToApply;
    }

    /**
     * Event that occur when the file is choose on the front (Files the boxes as
     * a preview of the file to upload and lock/unlocks the button to upload and
     * to cancel)
     */
    //fix Indefinido string
    //fix result value -> validateFileName(String fileName) leave it in his place or take to UTILS
    public boolean onFileChange(boolean result) {
        if (!result) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, Constants.INCORRECTFILE, Constants.INCORRECTFILEDESC);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            this.isSubmitDisable = true;
            this.isOperationOccuring = true;
            return false;
        } else {
            List<IO> operationsRunning = OperationRunningDAL.getOperationRunningFromIO(getModuleVersionExecution(), getDomainExecution(), getEntityExecution(), getReferenceDate().format(Constants.dateFormat));
            /*List<IO> operationsRunning = OperationRunningDAL.getOperationRunningFromIO(getModuleVersionExecution(), getDomainExecution(), getReferenceDate().format(Constants.dateFormat));*/
            if (!operationsRunning.isEmpty()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Operacoes a ser realizadas ", "Encontram-se a realizar operacoes relacionadas com o ficheiro escolhido. Tente mais tarde, contacte um administrador ou cancele a operacao a ser realizada");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                this.isOperationOccuring = false;
                this.isSubmitDisable = true;
                return false;
            }
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Ficheiro pronto", "Ficheiro encontra-se pronto para ser importado");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            this.isSubmitDisable = false;
            this.isOperationOccuring = true;
            return true;
        }
    }

    // Validate if the filename respects the format ENTITY_MODULE_DOMAIN_DATE.xlsx/csv
    public boolean validateFileName(String fileName) {
        if (fileName == null || fileName.equals("") || !fileName.matches(Constants.IMPORT_REGEX)) {
            setStatusMessage("Ficheiro não selecionado");
            return false;
        }
        try {
            String fileExtension = Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_9);
            if (fileExtension.equals("")) {
                setStatusMessage(Constants.extensaoInvalida);
                return false;
            }

            LocalDate referenceDate = Utils.stringToDate(Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_5), Constants.dateFormat);
            if (referenceDate == null) {
                setStatusMessage(Constants.dataInvalida);
                return false;
            }
            String year = Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_6);
            String month = Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_7);
            ModuleVersion moduleVersion = getModuleByFilenameInfo(referenceDate, Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_3));
            ConfEntities entity = getEntityByFilenameInfo(Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_2));
            String domain = Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_4);

            if (moduleVersion == null) {
                setStatusMessage(Constants.moduloInvalido);
                return false;
            } else if (entity == null) {
                setStatusMessage(Constants.entidadeInvalida);
                return false;
            } else if (domain.equals("")) {
                setStatusMessage(Constants.dominioInvalido);
                return false;
            } else {
                setYearExecution(Integer.valueOf(year));
                setMonthExecution(Integer.valueOf(month));
                setModuleVersionExecution(moduleVersion);
                setEntityExecution(entity);
                setDomainExecution(domain);
                setReferenceDate(Integer.valueOf(year), Integer.valueOf(month));
            }
            return true;
        } catch (Exception e) {
            LOG.error("Importacao | Erro no processo de validacao do nome do ficheiro.", e);
            return false;
        }
    }

    // Upload file to a local directory or to the application's server
    public void uploadFile(MultipartFile uploadedFile) {
        setStatusMessage("");
        List<ConfAppConfigs> configs = new ArrayList<>();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File inputFile = null;
        String path = null;
        int read = 0;

        // Copy uploaded file to destination path
        try {
            configs = Info.getInstance().refDataGet(Constants.AppConfigsAll);
            path = configs.stream().filter(x -> x.getKey().equals(Constants.IMPORTFILESPATH)).findFirst().get().getValue();
            String uniqueFileName = getNewFileName(uploadedFile.getOriginalFilename());
            inputFile = new File(path + Utils.getSeparator() + uniqueFileName);
            inputStream = uploadedFile.getInputStream();
            outputStream = new FileOutputStream(inputFile);
            final byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            setStatusMessage("Ficheiro " + uploadedFile.getOriginalFilename() + " carregado com sucesso para a diretoria de importacao.");
            startImportOperation(inputFile, uploadedFile.getOriginalFilename(), uniqueFileName);
        } catch (Exception e) {
            LOG.error("Erro uploadFile " + uploadedFile.getOriginalFilename() + ".", e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Upload de Ficheiro falho", "Falha na importacao do ficheiro para a diretoria de importacao!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            setStatusMessage("Falha na importacao do ficheiro para a diretoria de importacao!");
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                LOG.error("Erro ao fechar streams.", e);
            }
        }
    }

    public void startImportOperation(File file, String originalFileName, String filenameOnServer) {
        List<Integer> aux = Stream.concat(getListOfImportRulesToApply().stream(), listOfImportRulesToAlwaysApply.stream())
                .collect(Collectors.toList());
        //Thread t = new Thread(new ModuleFileImport(file, originalFileName, getEntityExecution(), getDomainExecution(), getReferenceDate().format(Constants.dateFormat), getModuleVersionExecution(), filenameOnServer, aux));
        //Thread t = new Thread(new ModuleFileImport(file, originalFileName, getDomainExecution(), getReferenceDate().format(Constants.dateFormat), getModuleVersionExecution(), filenameOnServer, aux));
        //t.setName(Constants.IMPORT + filenameOnServer);
        //t.start();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getIsSubmitDisable() {
        return isSubmitDisable;
    }

    public void setIsSubmitDisable(Boolean isSubmitDisable) {
        this.isSubmitDisable = isSubmitDisable;
    }

    public Boolean getIsOperationOccuring() {
        return isOperationOccuring;
    }

    public void setIsOperationOccuring(Boolean isOperationOccuring) {
        this.isOperationOccuring = isOperationOccuring;
    }

    public List<IO> getImportIOs() {
        return importIOs;
    }

    public void setImportIOs(List<IO> importIOs) {
        this.importIOs = importIOs;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    /**
     * method used to convert the filename, to be unique a keep it in the server
     *
     * @param oldFilename the nameOftheFile the user Keept
     * @return the new filename/ the filename the file will have in the server
     */
    public String getNewFileName(String oldFilename) {
        int indexToRemoveExtension = oldFilename.lastIndexOf(Constants.DOTSEPARATOR);
        String filenameWhitoutExtension = oldFilename.substring(Constants.FIRSTOCCURENCE, indexToRemoveExtension);
        String extension = oldFilename.substring(indexToRemoveExtension);
        return new StringBuilder(filenameWhitoutExtension)
                .append(Constants.UNDERSCORESEPARATOR)
                .append(Utils.getCurrentTimeStampAsString(Constants.TIMEDATEFORMATTER))
                .append(extension)
                .toString();
    }

    // Metodo modificado para Thymeleaf
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(String filename) {
        List<ConfAppConfigs> configs = new ArrayList<>();
        InputStream inputStream = null;
        InputStreamResource resource = null;
        String path = null;
        File inputFile = null;
        try {
            configs = Info.getInstance().refDataGet(Constants.AppConfigsAll);
            path = configs.stream().filter(x -> x.getKey().equals(Constants.IMPORTFILESPATH)).findFirst().get().getValue();
            inputFile = new File(path + Utils.getSeparator() + filename);
            if (inputFile.exists()) {
                inputStream = new FileInputStream(inputFile);
                resource = new InputStreamResource(inputStream);
            }

            if (inputStream != null) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(resource);
            } else {
                return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ficheiro não se encontra no servidor.");
            }
        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao preparar o ficheiro.");
        }
    }
}
