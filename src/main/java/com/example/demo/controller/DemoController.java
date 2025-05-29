package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.DTOs.OutValidationsDashboardDTO;
import com.example.demo.DTOs.ValidationResultsDetailsDTO;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.ConfImportRules;
import com.example.demo.controller.Objects.Constants;
import com.example.demo.controller.Objects.DefaultBean;
import com.example.demo.controller.Objects.ImportFileController;
import com.example.demo.controller.Objects.Info;
import com.example.demo.controller.Objects.Month;
import com.example.demo.controller.Objects.Template;
import com.example.demo.controller.Objects.Year;
import com.example.demo.controller.Objects.DAL.IODAL;
import com.example.demo.controller.Objects.Entities.IO;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.springframework.ui.Model;

import java.sql.*;
import java.time.LocalDate;
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

        LOG.info("What");

        createTables();
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
    public ModelAndView greeting() {
        ModelAndView modelAndView = new ModelAndView();

        init();
        /*JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();

        try {
            results = jpa.getTypedNativeResultList("DetalhesValidacoes.sql");
            modelAndView.addObject("results", results);
        } catch (Exception e) {
            LOG.error("Erro na query ValidationResults no Download XBRL:" + e.getMessage());
            e.printStackTrace();
        }*/
        

        modelAndView.setViewName("index");
        modelAndView.addObject("username", "Marcus Tremor"); // Dynamic username
        return modelAndView; // Thymeleaf template name (greeting.html)
    }

    @GetMapping("/thymeleaf")
    public ModelAndView thymeleafPage(Model model) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("thymeleaf");
        modelAndView.addObject("user", "Alex"); // Default value

        return modelAndView;
    }

    @GetMapping("/index_Neyond")
    public ModelAndView indexNeyond() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index_Neyond");
        return modelAndView;
    }

    @GetMapping("/download_template2")
    public ModelAndView downloadTemplate2() {        
        ModelAndView modelAndView = new ModelAndView();
        directory = "jdbc:sqlite:UNMANAGEDPROCESS.db";
        String sqlQuery = "SELECT * FROM tabelaTeste;";
        List<Template> templates = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(directory);
            Statement statement = connection.createStatement()) {
                
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            while (resultSet.next()) {
                templates.add(new Template(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("description")));
                System.out.println("Templates List: " + templates);
                for (Template t : templates) {
                    System.out.println("Template ID: " + t.getId() + ", Name: " + t.getName() + ",  Description: " + t.getDescription());
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        modelAndView.addObject("templates", templates);
        System.out.println("Added Templates");
        modelAndView.setViewName("download_template");
        return modelAndView;
    }

    @GetMapping("/download_template")
    public ModelAndView downloadTemplate() {        
        ModelAndView modelAndView = new ModelAndView();
        directory = "jdbc:sqlite:UNMANAGEDPROCESS.db";
        String sqlQuery = "SELECT * FROM [Category];";
        List<Template> templates = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(directory);
            Statement statement = connection.createStatement()) {
                
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            while (resultSet.next()) {
                templates.add(new Template(resultSet.getInt("CategoryID"), resultSet.getString("Name"), resultSet.getString("Description")));
                /*for (Template t : templates) {
                    System.out.println("Template ID: " + t.getId() + ", Name: " + t.getName() + ",  Description: " + t.getDescription());
                }*/
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        modelAndView.addObject("templates", templates);
        //System.out.println("Added Templates");
        modelAndView.setViewName("download_template");
        return modelAndView;
    }

    

    @GetMapping("/import_file")
    public ModelAndView importFile() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("import_file");
        return modelAndView;
    }

    @GetMapping("/importFileDetail")
    public ModelAndView importFileDetail() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("import_file_detail");
        return modelAndView;
    }

    @GetMapping("/import_fileTest")
    public ModelAndView importFileTest() {
        
        List<String[]> columnValues = new ArrayList<>();
        directory = "jdbc:sqlite:UNMANAGEDPROCESS.db";
        String sqlQuery = "INSERT INTO tabelaTeste (Name, Description) VALUES (?, ?);";
        try (
            // Connect to Azure SQL Server
            /*Connection azureConn = DriverManager.getConnection(AZURE_URL, AZURE_USERNAME, AZURE_PASSWORD);
            Statement stmt = azureConn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Name, Description FROM Category");*/

            // Connect to SQLite
            Connection sqliteConn = DriverManager.getConnection(directory);
            PreparedStatement pstmt = sqliteConn.prepareStatement(sqlQuery)
        ) {
            // Fetch column values from Azure SQL Server
            /*while (rs.next()) {
                String value1 = rs.getString(1);
                String value2 = rs.getString(2);
                columnValues.add(new String[]{value1, value2});

                // Insert both values into SQLite
                pstmt.setString(1, value1);
                pstmt.setString(2, value2);
                pstmt.executeUpdate();
            }*/

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database operation failed: " + e.getMessage());
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("import_file");
        return modelAndView;
    }

    @GetMapping("/run_validations")
    public ModelAndView runValidations() {
        Year year = new Year();
        Month month = new Month();
        List<Integer> years = year.getYears();
        List<String> months = month.getMonths();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("getYears", years);
        modelAndView.addObject("year", year.getCurrentYear());
        modelAndView.addObject("getMonths", months);
        modelAndView.setViewName("run_validations");
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