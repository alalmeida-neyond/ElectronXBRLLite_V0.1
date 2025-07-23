package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.DTOs.ValidationResultsDetailsDTO;
import com.example.demo.Data.Access.Info;
import com.example.demo.Resources.Constants;
import com.example.demo.Verification.LicenseVerification;
import com.example.demo.controller.Objects.Beans.DefaultBean;
import com.example.demo.controller.Objects.Entities.Conf.ConfImportRules;
import com.example.demo.service.ValidationService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import java.sql.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
public class DemoController extends DefaultBean{
    private String directory;

    private final String fileUrl = "https://blobstoragexbrl.blob.core.windows.net/xbrldatabaseblob/UNMANAGEDPROCESS.db";
    private final String localFilePath = System.getProperty("user.dir") + File.separator + "/src/UNMANAGEDPROCESS.db"; 
    private final Logger LOG = Logger.getLogger(ImportFileController.class);
    private List<Integer> listOfImportRulesToApply = new ArrayList<Integer>();
    private List<Integer> listOfImportRulesToAlwaysApply = new ArrayList<Integer>();
    private List<ConfImportRules> listOfImportRules;
    private static boolean licenseValidated = false;
    @Autowired
    private LicenseVerification licenseVerification;

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

        createTables();
    }

    

    // Create tables if they don't exist
    private void createTables() {
        directory = "jdbc:sqlite:UNMANAGEDPROCESS.db";
        String sqlQuery = "CREATE TABLE IF NOT EXISTS tabelaTeste (\n"
                        + " id integer PRIMARY KEY,\n"
                        + " name text NOT NULL,\n"
                        + " description text \n"
                        + ");";
        
        try (Connection connection = DriverManager.getConnection(directory);
            Statement statement = connection.createStatement()) {
                
            statement.execute(sqlQuery);
            statement.execute("DROP TABLE tabelaTeste;");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

        init();
        licenseValidated = licenseVerification.licenseValidationFile();
        modelAndView.addObject("LEICode", licenseVerification.getLEICode());
        if (licenseValidated) {
            modelAndView.setViewName("test");
        } else {
            modelAndView.setViewName("licensepage");
            LOG.info(licenseVerification.isExpired());
            if (licenseVerification.isExpired()) {
                modelAndView.addObject("expired", true);
            }
        }
        //modelAndView.setViewName("index");
        
        return modelAndView;
    }

    @GetMapping("/settings")
    public ModelAndView settings() {
        ModelAndView modelAndView = new ModelAndView();
        try {
            licenseValidated = licenseVerification.licenseValidationFile();
            modelAndView.addObject("LEICode", licenseVerification.getLEICode());
        } catch (Exception e) {
            licenseValidated = false;
        }
        if (licenseValidated)
        {
            modelAndView.setViewName("settings");
        }
        else
        {
            modelAndView.setViewName("licensepage");
        }
        return modelAndView;
    }

    @GetMapping("/licensing")
    public ModelAndView licensing() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("licensepage");
        return modelAndView;
    }

    @PostMapping("/licensing")
    public ModelAndView insertingLicense(@RequestParam("licensingString") String licensingString) {
        try {
            licenseVerification.licenseVerificationString(licensingString);
            return new ModelAndView("redirect:/");

        } catch (Exception e) {
           ModelAndView mv = new ModelAndView("licensepage");
            mv.addObject("error", "Invalid license: " + e.getMessage());
            return mv;
        }
    }

    @PostMapping("/renewLicense")
    public ModelAndView renewLicense() {
        try {
            return new ModelAndView("redirect:/");

        } catch (Exception e) {
           ModelAndView mv = new ModelAndView("licensepage");
            mv.addObject("error", "Invalid license: " + e.getMessage());
            return mv;
        }
    }

    @PostMapping("/requestLicense")
    public void requestingLicense(@RequestParam String inputEmailText,
        @RequestParam String inputLEICODEText,
        @RequestParam String inputHardwareIDText,
        @RequestParam String inputBDPIDText,
        @RequestParam String inputLicenseType ) {
        LOG.info("Email:" + inputEmailText);
        LOG.info("LEI CODE:" + inputLEICODEText);
        LOG.info("Hardware ID:" + inputHardwareIDText);
        LOG.info("BDP ID:" + inputBDPIDText);
        LOG.info("License Type:" + inputLicenseType);
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

    @GetMapping("/importFile/results/{id}")
    @ResponseBody
    public List<ValidationResultsDetailsDTO> getValidationResults(@PathVariable("id") Integer ioId) {
        LOG.info("Validation");
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
        LOG.info("Modules");
        ValidationService validationService = new ValidationService();
        List<String> validationResults = new ArrayList<String>();
        validationResults = validationService.getModules();
        return validationResults;
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

    @PostMapping("/processMetaData")
    public void handleMetaDataTransfer(Model model) {
        try {
            
            URL url = URI.create(fileUrl).toURL();
            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, Path.of(localFilePath), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error replacing file: " + e.getMessage());
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
}