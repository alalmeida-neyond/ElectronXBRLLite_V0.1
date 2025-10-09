package com.example.demo.controller.Objects.Beans;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.DTOs.ValidationResultsDetailsDTO;
import com.example.demo.Data.Access.Info;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.Verification.LicenseVerification;
import com.example.demo.controller.Objects.Entities.Conf.ConfAppConfigs;
import com.example.demo.controller.Objects.Entities.Conf.ConfEntities;
import com.example.demo.controller.Objects.Entities.Conf.ConfImportRules;
import com.example.demo.controller.Objects.Entities.Conf.ConfTemplate;
import com.example.demo.controller.Objects.Entities.DAL.ConfTemplateDAL;
import com.example.demo.controller.Objects.Entities.DAL.IODAL;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.IO.IO;
import com.example.demo.service.ModuleFileImport;
import com.example.demo.service.ProgressService;
import com.example.demo.service.ValidationService;
import org.springframework.http.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.ui.Model;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

@RestController
public class MainBean extends DefaultBean{
    private String directory;

    private final String fileUrl = Constants.fileUrl;
    private final String localFilePath = Constants.localFilePath;
    private final Logger LOG = Logger.getLogger(MainBean.class);
    private List<Integer> listOfImportRulesToApply = new ArrayList<Integer>();
    private List<Integer> listOfImportRulesToAlwaysApply = new ArrayList<Integer>();
    private List<ConfImportRules> listOfImportRules = new ArrayList<>();
    private static boolean licenseValidated = false;

    private List<IO> importIOs;

    private String fileName;
    private Boolean isSubmitDisable = true;
    private Boolean isOperationOccuring = true;
    private MultipartFile file;

    private static final String UPLOAD_DIR = Constants.UPLOAD_DIR;
    private final ProgressService progressService;

    public MainBean(ProgressService progressService) {
        this.progressService = progressService;
    }
    @Autowired
    private LicenseVerification licenseVerification;


    @PostConstruct
    public void init() {
        Info.getInstance().loadRefData(true);
        List<ConfImportRules> rules = Info.getInstance().refDataGet(Constants.ConfImportRulesAll);
        if (rules == null) {
            rules = new ArrayList<>();
        }
        setListOfImportRules(rules);

        listOfImportRulesToApply = getListOfImportRules().stream()
                .map(ConfImportRules::getImportRuleID)
                .collect(Collectors.toList());

        listOfImportRulesToAlwaysApply = getListOfImportRules().stream()
                .filter(p -> p.isAlwaysRun() == true)
                .map(ConfImportRules::getImportRuleID)
                .collect(Collectors.toList());

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
                getEntity() == null ? null : getEntity().getEntityID(), getDomain(), Constants.actionImport);

        setImportIOs(importIOs);

    }

    public void upload() {
        if (file != null) {
            if (onFileChange(validateFileName(getFile().getOriginalFilename()))) {
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
        if (fileName == null || fileName.equals("") || !fileName.matches(Constants.IMPORT_REGEX)) {
            if(!fileName.matches(Constants.IMPORT_REGEX))
            {
                setStatusMessage(Constants.INCORRECTFILEDESC);
            }
            else
            {
                setStatusMessage("Ficheiro não selecionado");
            }
            
            return false;
        }
        try {
            String fileExtension = Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_9);
            if (fileExtension.equals("")) {
                setStatusMessage(Constants.extensaoInvalida);
                return false;
            }

            LocalDate referenceDate = Utils.stringToDate(
                    Utils.findOneByRegex(Constants.IMPORT_REGEX, fileName, Constants.REGEX_GROUP_5),
                    Constants.dateFormat);
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
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File inputFile = null;
        Path path = null;
        int read = 0;

        try {
            path = Paths.get("XBRL_Lite", "Run", "Reports", "DataPoints");
            String uniqueFileName = getNewFileName(uploadedFile.getOriginalFilename());
            inputFile = new File(path + Utils.getSeparator() + uniqueFileName);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        Files.delete(file);
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
                getReferenceDate(), getModuleVersionExecution(), filenameOnServer, aux, progressService);

        //progressService.setImportProgress(20);

        importExecution.run();

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
    
    @PostMapping("/importFile/upload")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        
        try {
            setFile(file);

            if (!validateFileName(file.getOriginalFilename())) {
                return ResponseEntity.badRequest().body(getStatusMessage());
            }
            //progressService.setImportProgress(10);
            upload();
            return ResponseEntity.ok("File uploaded successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

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

    @GetMapping("/importProgress")
    public int getImportProgress() {
        return progressService.getImportProgress();
    }

    @GetMapping("/validationProgress")
    public int getValidationProgress() {
        return progressService.getValidationProgress();
    }

    @GetMapping("/generationProgress")
    public int getGenerationProgress() {
        return progressService.getGenerationProgress();
    }

    @PersistenceUnit
    private EntityManagerFactory emf;

    @PreDestroy
    public void closeEntityManager() {
        if (emf != null) {
            emf.close();
            System.out.println("EntityManagerFactory closed.");
        }
    }
    

    @GetMapping("/")
    public ModelAndView greeting() throws FileNotFoundException {
        ModelAndView modelAndView = new ModelAndView();

        //init();
        modelAndView.addObject(Constants.LEICodeKeyString, licenseVerification.getLEICode());

        modelAndView.setViewName("test");
        
        
        return modelAndView;
    }

    @GetMapping("/settings")
    public ModelAndView settings() {
        ModelAndView modelAndView = new ModelAndView();

        String storedPath = getStoredPathOrFallback();
        modelAndView.addObject("storedPath", storedPath);
       
        modelAndView.setViewName("settings");
       
        return modelAndView;
    }

    @GetMapping("/templates")
    public ModelAndView templates(@RequestParam(required = false) Integer moduleVid,@RequestParam(required = false) Integer year,@RequestParam(required = false) Integer month, @RequestParam(required = false) String version) {
        ModelAndView modelAndView = new ModelAndView();
        List<ConfTemplate> listOfTemplates;

        String referenceDate = null;
        LocalDate dateaux = null;
        if (year != null && month != null) {
            referenceDate = getDateAtLastDayFormatted(getYear(), getMonth());
            dateaux = LocalDate.parse(referenceDate,Constants.dateFormat);
        }
        listOfTemplates = ConfTemplateDAL.getTemplates(moduleVid != null ? moduleVid + "":null, dateaux, version != null ? version + "":null);

        modelAndView.addObject("currentModuleVid", moduleVid);
        modelAndView.addObject("currentYear", year);
        modelAndView.addObject("currentMonth", month);
        modelAndView.addObject("currentVersion", version);

        modelAndView.addObject("templateList", listOfTemplates);
            
        return modelAndView;
    }
    @PostMapping("/templates/download")
    public ResponseEntity<Map<String,Object>> downloadByFilename(
            @RequestParam("filename") String filename) {
        Map<String,Object> body = new HashMap<>();
        try {
            if (filename == null || filename.isBlank()) {
                body.put("ok", false);
                body.put("message", "Nome do ficheiro em falta.");
                return ResponseEntity.badRequest().body(body);
            }

            String safeName = Paths.get(filename).getFileName().toString();

            Path sourceBase = Paths.get("AdditionalFiles").toAbsolutePath().normalize();
            Path source = sourceBase.resolve(safeName).normalize();

            if (!source.startsWith(sourceBase)) {
                body.put("ok", false);
                body.put("message", "Nome de ficheiro inválido.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
            }

            if (!Files.exists(source) || !Files.isRegularFile(source)) {
                body.put("ok", false);
                body.put("message", "Ficheiro não encontrado em AdditionalFiles.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
            }

            String destDir = getStoredPathOrFallback();
            Path destBase = Paths.get(destDir).toAbsolutePath().normalize();
            Files.createDirectories(destBase);

            Path dest = destBase.resolve(safeName).normalize();
            if (!dest.startsWith(destBase)) {
                body.put("ok", false);
                body.put("message", "Destino inválido.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
            }

            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);

            body.put("ok", true);
            body.put("message", "Ficheiro copiado para: " + dest.toAbsolutePath());
            return ResponseEntity.ok(body);

        } catch (Exception ex) {
            Map<String,Object> err = new HashMap<>();
            err.put("ok", false);
            err.put("message", "Erro ao copiar o ficheiro.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    private String getStoredPathOrFallback() {
        try {
            Path filePath = Paths.get("path.dat");

            if (Files.exists(filePath)) {
                String content = Files.readString(filePath).trim();
                if (!content.isEmpty()) {
                    Path dir = Paths.get(content);
                    if (!Files.exists(dir)) {
                        Files.createDirectories(dir);
                    }
                    return dir.toAbsolutePath().toString();
                }
            }
        } catch (IOException e) {
            LOG.error("Erro ao ler/criar diretoria do path.dat", e);
        }

        Path fallback = Paths.get(System.getProperty("user.home"), "Downloads");
        try {
            if (!Files.exists(fallback)) {
                Files.createDirectories(fallback);
            }
        } catch (IOException e) {
            LOG.error("Erro ao criar pasta Downloads", e);
        }
        return fallback.toAbsolutePath().toString();
    }

    @GetMapping("/import_file")
    public ModelAndView importFile() throws FileNotFoundException {
        
        ModelAndView modelAndView = new ModelAndView();
        try {
            licenseValidated = licenseVerification.licenseValidationFile();
        } catch (Exception e) {
            licenseValidated = false;
            System.err.println("Erro ao validar licença: " + e.getMessage());
        }
        if (licenseValidated)
        {
            modelAndView.setViewName("import_file");
        }
        else
        {
            modelAndView.setViewName("licensepage");
        }
        return modelAndView;
    }

    @GetMapping("/importFile/validation/results/{id}")
    @ResponseBody
    public List<ValidationResultsDetailsDTO> getValidationResults(@PathVariable("id") Integer ioId) {
        ValidationService validationService = new ValidationService();
        return validationService.getValidationResults(ioId);
    }
    @GetMapping("/importFile/import/results/{id}")
    @ResponseBody
    public List<ValidationResultsDetailsDTO> getImportResults(@PathVariable("id") Integer ioId) {
        ValidationService validationService = new ValidationService();
        return validationService.getValidationResults(ioId);
    }
    @GetMapping("/importFile/generation/results/{id}")
    @ResponseBody
    public List<ValidationResultsDetailsDTO> getGenerationResults(@PathVariable("id") Integer ioId) {
        ValidationService validationService = new ValidationService();
        return validationService.getValidationResults(ioId);
    }

    @GetMapping("/importFile/results")
    @ResponseBody
    public List<Object[]> getIOResults() {
        ValidationService validationService = new ValidationService();
        List<Object[]> validationResults = new ArrayList<Object[]>();
        validationResults = validationService.getIOResults();
        return validationResults;
    }

    @GetMapping("/importFile/modules")
    @ResponseBody
    public List<String> getModules() {
        ValidationService validationService = new ValidationService();
        List<String> validationResults = new ArrayList<String>();
        validationResults = validationService.getModules();
        return validationResults;
    }

    @GetMapping("/importFile/versions")
    @ResponseBody
    public List<String> getVersionsPage() {
        List<String> versions = new ArrayList<String>();
        versions = getVersions();
        return versions;
    }


    @GetMapping("/importFileDetail")
    public ModelAndView importFileDetail() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("import_file_detail");
        return modelAndView;
    }

    @GetMapping("/generate_xbrl")
    public ModelAndView generateXBRL() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index_Neyond");
        return modelAndView;
    }
}