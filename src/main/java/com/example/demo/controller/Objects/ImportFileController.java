package com.example.demo.controller.Objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.jboss.logging.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;

import com.example.demo.controller.Objects.DAL.IODAL;
import com.example.demo.controller.Objects.Entities.*;
import com.example.demo.service.ModuleFileImport;

@RestController
@RequestMapping("/importFile")
public class ImportFileController extends DefaultBean {

    private final Logger LOG = Logger.getLogger(ImportFileController.class);

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
        setListOfImportRules(Info.getInstance().refDataGet(Constants.ConfImportRulesAll));
        listOfImportRulesToApply = getListOfImportRules().stream()
                .map(ConfImportRules::getImportRuleID)
                .collect(Collectors.toList());

        listOfImportRulesToAlwaysApply = getListOfImportRules().stream()
                .filter(p -> p.isAlwaysRun() == true)
                .map(ConfImportRules::getImportRuleID)
                .collect(Collectors.toList());

        getImportIOs(refData.getImportID(), true);
    }

    public void getImportIOs(Integer privilege, boolean withView) {
        LocalDate referenceDate = null;
        if (getYear() != null && getMonth() != null) {
            // CastMonth into number
            //setReferenceDate(getYear(), getMonth());
            referenceDate = getDateAtLastDay(getMonth(),getYear());
        } else {
            referenceDate = null;
        }

        importIOs = IODAL.getIOsByAction(getModuleVersion() == null ? null : getModuleVersion().getModuleVID(),
                referenceDate,
                getEntity() == null ? null : getEntity().getEntityID(), getDomain(), Constants.actionImport,
                Constants.VALIDATEID);

    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            setFile(file);
            // LOG.info("Successful\n" + file.getOriginalFilename() + " is uploaded.");
            upload();
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
        
    }

    public void upload() {
        // LOG.warn("File Name:"+getFile().getOriginalFilename());
        if (file != null) {
            // LOG.warn("Not Null");
            if (onFileChange(validateFileName(getFile().getOriginalFilename()))) {
                // LOG.info("Ficheiro encontra-se pronto para ser importado");

                try {
                    uploadFile(getFile());
                } catch (Exception e) {
                    LOG.error("Erro no upload do ficheiro para o servidor.", e);
                }
            }

        }
    }

    @GetMapping("/api/rules")
    public List<ConfImportRules> getListOfImportRules() {
        System.out.println("Número de regras:" + listOfImportRules.size());
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
    // fix Indefinido string
    // fix result value -> validateFileName(String fileName) leave it in his place
    // or take to UTILS
    public boolean onFileChange(boolean result) {
        if (!result) {
            LOG.warn(Constants.INCORRECTFILE + "\n" + Constants.INCORRECTFILEDESC);
            this.isSubmitDisable = true;
            this.isOperationOccuring = true;
            return false;
        } else {
            this.isSubmitDisable = false;
            this.isOperationOccuring = true;
            return true;
        }
    }

    // Validate if the filename respects the format
    // ENTITY_MODULE_DOMAIN_DATE.xlsx/csv
    public boolean validateFileName(String fileName) {
        LOG.info("Filename:" + fileName);
        if (fileName == null || fileName.equals("") || !fileName.matches(Constants.IMPORT_REGEX)) {
            setStatusMessage("Ficheiro não selecionado");
            return false;
        }
        try {
            String fileExtension = Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_9);
            LOG.info("File Extension:" + fileExtension);
            if (fileExtension.equals("")) {
                setStatusMessage(Constants.extensaoInvalida);
                return false;
            }

            LocalDate referenceDate = Utils.stringToDate(
                    Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_5),
                    Constants.dateFormat);
            LOG.info("Reference Date:" + referenceDate);
            if (referenceDate == null) {
                setStatusMessage(Constants.dataInvalida);
                return false;
            }
            String year = Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_6);
            String month = Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_7);
            ModuleVersion moduleVersion = getModuleByFilenameInfo(referenceDate,
                    Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_3));
            ConfEntities entity = getEntityByFilenameInfo(
                    Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_2));
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
        Path path = null;
        int read = 0;

        try {
            configs = Info.getInstance().refDataGet(Constants.AppConfigsAll);
            // path = configs.stream().filter(x ->
            // x.getKey().equals(Constants.IMPORTFILESPATH)).findFirst().get().getValue();
            path = Paths.get("XBRL_Lite", "Run", "Reports", "DataPoints");
            // LOG.info("Path:" + path);
            String uniqueFileName = getNewFileName(uploadedFile.getOriginalFilename());
            inputFile = new File(path + Utils.getSeparator() + uniqueFileName);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        Files.delete(file);
                        LOG.info("Deleted: " + file.getFileName());
                    }
                }
            } catch (IOException e) {
                LOG.error("Error deleting files: " + e.getMessage());
            }

            inputStream = uploadedFile.getInputStream();
            outputStream = new FileOutputStream(inputFile);
            final byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            setStatusMessage("Ficheiro " + uploadedFile.getOriginalFilename()
                    + " carregado com sucesso para a diretoria de importacao.");
            /*
             * LOG.info("InputFile:" + inputFile);
             * LOG.info("OriginalFilename:" + uploadedFile.getOriginalFilename());
             * LOG.info("uniqueFileName:" + uniqueFileName);
             */
            startImportOperation(inputFile, uploadedFile.getOriginalFilename(), uniqueFileName);
        } catch (Exception e) {
            LOG.error("Erro uploadFile " + uploadedFile.getOriginalFilename() + ".", e);
            LOG.error(
                    "Upload de Ficheiro falhou.\n" + "Falha na importacao do ficheiro para a diretoria de importacao!");
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

        List<Integer> aux = Stream
                .concat(getListOfImportRulesToApply().stream(), listOfImportRulesToAlwaysApply.stream())
                .collect(Collectors.toList());

        ModuleFileImport importExecution = new ModuleFileImport(file, originalFileName, getEntityExecution(), getDomainExecution(),
                getReferenceDate(), getModuleVersionExecution(), filenameOnServer, aux);

        importExecution.run();

        /*Thread t = new Thread(new ModuleFileImport(file, originalFileName, getEntityExecution(), getDomainExecution(),
                getReferenceDate(), getModuleVersionExecution(), filenameOnServer, aux));
        // Thread t = new Thread(new ModuleFileImport(file, originalFileName,
        // getDomainExecution(), getReferenceDate().format(Constants.dateFormat),
        // getModuleVersionExecution(), filenameOnServer, aux));
        t.setName(Constants.IMPORT + filenameOnServer);*/
        /*
         * LOG.info("Size:" + aux.size());
         * //LOG.info("Entity:" + getEntityExecution().getDescription());
         * LOG.info("DomainExecution:" + getDomainExecution());
         * LOG.info("ReferenceDate:" + getReferenceDate().format(Constants.dateFormat));
         * LOG.info("ModuleVersion:" + getModuleVersionExecution().getName());
         */
        /*t.start();*/
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
            path = configs.stream().filter(x -> x.getKey().equals(Constants.IMPORTFILESPATH)).findFirst().get()
                    .getValue();
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
