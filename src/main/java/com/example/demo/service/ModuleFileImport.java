package com.example.demo.service;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;
import com.example.demo.DTOs.*;
import com.example.demo.Data.*;
import com.example.demo.Data.Access.Info;
import com.example.demo.Finrep.*;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Entities.Conf.*;
import com.example.demo.controller.Objects.Entities.DAL.*;
import com.example.demo.controller.Objects.Entities.DPMOrigin.Cell;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ItemCategory;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.Entities.DPMOrigin.TableVersionDPM;
import com.example.demo.controller.Objects.Entities.DPMOrigin.VariableVersion;
import com.example.demo.controller.Objects.Entities.Logs.LogImportProcess;
import com.example.demo.controller.Objects.IO.IO;
import com.example.demo.controller.Objects.IO.IOState;
import com.example.demo.controller.Objects.IO.IOTypeState;
import com.example.demo.controller.Objects.Import.*;
import com.example.demo.controller.Objects.Validation.Validator_2_0;
import com.example.demo.controller.Objects.Extensions.RunnableExtension;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;

import java.util.ArrayList;
import java.util.Arrays;

public class ModuleFileImport extends RunnableExtension{
    
    //main constructor to use in Import
    public ModuleFileImport(File inputFile, String filename, ConfEntities entity, String domain,LocalDate referenceDate,ModuleVersion moduleVersion, 
    String filenameWithTimestamp, List<Integer> ruleIdsToApply, ProgressService progressService) {
        this.inputFile = inputFile;
        this.filename = filename;
        this.entity = entity;
        this.domain = domain;
        this.referenceDate = referenceDate;
        this.moduleVersion = moduleVersion;
        this.alterFilename = filenameWithTimestamp;
        this.listOfImportRulesToApply = ruleIdsToApply;
        this.progressService = progressService;
    }
    
    private List<Integer> typeListToExclude = Arrays.asList(Constants.INTEGER, Constants.DECIMAL, Constants.MONETARY, Constants.PERCENTAGE);
    private File inputFile;
    private String filename;
    private ConfEntities entity;
    private String domain;
    private LocalDate referenceDate;
    private ModuleVersion moduleVersion;
    private String alterFilename;
    private List<Integer> listOfImportRulesToApply;
    private final Logger LOG = Logger.getLogger(ModuleFileImport.class);

    private final ProgressService progressService;

            

    public String getAlterFilename() {
        return alterFilename;
    }

    public void setAlterFilename(String alterFilename) {
        this.alterFilename = alterFilename;
    }
    
    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }
    
    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    
    public ConfEntities getEntity() {
        return entity;
    }

    public void setEntity(ConfEntities entity) {
        this.entity = entity;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    } 
    
    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    /**
     * method to do the main import
     * return NOK, OK, CANCEL, depending if it conclude with success, without success or have been canceled
     */
    private IOState mainImport(IO io, List<Integer> listOfIDImportRules){

        progressService.setImportProgress(0,1);
        progressService.setValidationProgress(0,1);
        progressService.setGenerationProgress(0,1);
        EntityManager em = Connection.getEm();
        ConnectionManager cm = null;
        IOState ioState = null;
        
        boolean hasOk = false;
        boolean hasNotOk = false;
        boolean hasEmpty = false;
        try {
            cm = new ConnectionManager(em);
            ExcelPoints excelPointStruct = new ExcelPoints();
            FileInputStream fileStream = new FileInputStream(inputFile);
            XSSFWorkbook workBook = new XSSFWorkbook(fileStream);
            try{
                fileStream.close();
            }catch(Exception e){
                e.printStackTrace();
                LOG.error("Closing failed" + e);
            }
            //for each sheet in the imported file;

            List<VariableVersion> varVersionMapList = VariableVersionDAL.getListOfVariableVersionOfModuleSheets(moduleVersion.getModuleVID());
            List<TableVersionDPM> fillingIndicatorModuleList = TableVersionDAL.getAllFilesImported(moduleVersion,referenceDate);
            TableVersionDPM singleFillingIndicatorAsTableVersion = null;
            List<HeaderDTO> headerDTOList;
            List<CellVariableDTO> cellVariablesList;
            int nRegistos = 0;
            String rowValue = null;
            String columnValue = null;
            HeaderDTO rowValueAsHeaderCode;
            HeaderDTO columnValueAsHeaderCode;
            HeaderDTO sheetValueAsHeaderCode;
            InImportKey rowKeyImportKey;
            String value = null;
            String valueEdited = null;
            List<InImportedValuesTemp> openRowAuxListForPersiste = new ArrayList<>();
            boolean hasInsertedValue;
            List<String> errorMsgPerTables = new ArrayList<>();

            int totalNumberSheets = workBook.getNumberOfSheets();
            int completedTables = 1;
            progressService.setImportProgress(completedTables, Integer.valueOf(totalNumberSheets) + 1);
            for (int sheet = 0; sheet < workBook.getNumberOfSheets(); sheet++) {
                hasInsertedValue = false;
                //In case of Error, only required to upload sheets missing
                if (!em.getTransaction().isActive()) {
                    em.getTransaction().begin();
                }
                if(checkIfThredIsCanceled(em)){
                    ioState = new IOState(Constants.processoCanceled, new IOTypeState(Constants.tipoStateCanceled));
                    return ioState;
                }
                //Get Sheet
                XSSFSheet workSheet = workBook.getSheetAt(sheet);
                String sheetName = workSheet.getSheetName().trim();
                String fillingIndicator = sheetName;
                String desagregationCode = null;
                if(sheetName.contains("(")){
                    desagregationCode = sheetName.substring(sheetName.indexOf("(") + 1, sheetName.indexOf(")")).trim();
                    fillingIndicator = sheetName.substring(0, sheetName.indexOf("(")).trim();
                }
                //Filling Indicator like what is stored in the DB
                String fillingIndicatorWithUnderScores = fillingIndicator.replaceAll(" ", "_").trim();

                //Check if the filling indicator is possible
                singleFillingIndicatorAsTableVersion = FillingIndicatorModuleService.getTableVersionFromList(fillingIndicatorModuleList,fillingIndicatorWithUnderScores);
                if(singleFillingIndicatorAsTableVersion == null){
                    continue;
                }
                headerDTOList = TableVersionHeaderDAL.getListOfHeaderForATableVid(singleFillingIndicatorAsTableVersion.getTableVID());
                sheetValueAsHeaderCode = null;
                InImportKey desagregationCodeKey = null;
                if(desagregationCode != null){
                    if(Utils.isNumeric(desagregationCode)){
                            sheetValueAsHeaderCode = HeaderService.getHeaderDTOFromList(headerDTOList,desagregationCode.trim(),Constants.SheetCoordinateAsChar,false);
                        }
                    desagregationCodeKey = buildDesagregationCode(desagregationCode,singleFillingIndicatorAsTableVersion.getTableVID(),singleFillingIndicatorAsTableVersion.getTable().getTableId(), io.getReferenceDate(),cm);
                    
                    if (desagregationCodeKey != null && ((sheetValueAsHeaderCode != null && Utils.isNumeric(desagregationCode)) || !Utils.isNumeric(desagregationCode))) {
                        List<InKeyAssociation> keyAssociationsDesagCode = desagregationCodeKey.getListPropertyValues();
                        Optional<InKeyAssociation> keyAssociationWithNull = keyAssociationsDesagCode.stream().filter(keyAssociation -> keyAssociation.getPropertyValue() == null).findAny();

                        if (!keyAssociationWithNull.isPresent()) {
                            Connection.persist(cm, desagregationCodeKey);
                        }

                    }
                }
                VariableVersion variableVersionOfMap = null;
                if(singleFillingIndicatorAsTableVersion.getAbstractTable() != null){
                    variableVersionOfMap = VariableVersionDAL.getVariableVersionFromAbstract(singleFillingIndicatorAsTableVersion.getAbstractTable().getTableId());
                }else{
                    String codeTemp = singleFillingIndicatorAsTableVersion.getCode();
                    variableVersionOfMap = varVersionMapList.stream().filter(varVersion -> varVersion.getCode().equals(codeTemp)).findFirst().orElse(null);
                }
                
                InImportedTablesTemp importedTableTemp = new InImportedTablesTemp();
                importedTableTemp.setIo(io);
                importedTableTemp.setTableVersion(singleFillingIndicatorAsTableVersion);
                importedTableTemp.setVariableVersion(variableVersionOfMap);
                importedTableTemp.setImportKey(desagregationCodeKey);
                importedTableTemp.setInitTimestamp(LocalDateTime.now());
                importedTableTemp.setDesagregationCode(desagregationCode);
                importedTableTemp.setIoState(Info.getInstance().getIOStateByID(Constants.processoOkEmpty));                
                Connection.persist(cm, importedTableTemp);
                
                LogImportProcess logMapImportInit = new LogImportProcess(io.getIoId(),importedTableTemp.getImportedTableId(), "Importacao do mapa - " + sheetName + " iniciado");
                Connection.persist(cm, logMapImportInit);
                
                Map<String, Integer> properties = TableVersionDAL.getPropertiesKeyTypes(singleFillingIndicatorAsTableVersion.getTableVID(), io.getReferenceDate());
                
                boolean notInsertedInImportedValuesPerSheet = true;
                // Insert into Table Imported_Tables and get the Id Value
                ImportExcelValues.getExcelPoints(workSheet, excelPointStruct);
            
                cellVariablesList = CellDAL.getListOfCellVariable(singleFillingIndicatorAsTableVersion);
                for (int i = excelPointStruct.getFirstRow() - 1; i < excelPointStruct.getLastRow(); i++) {
                    try {
                        rowValue = ImportExcelValues.getCellValue(workSheet, i, excelPointStruct.getFirstColumn() - 2);
                    } catch (Exception e) {
                        //Erro nº da linha
                        errorMsgPerTables.add("Erro na obtencao da linha.");
                        continue;
                    }
                    List<DatapointItensDTO> ListItems = null;
                    TreeMap <String, List<DatapointItensDTO>> listItemsMapped = null;
                    rowKeyImportKey = null;
                    openRowAuxListForPersiste.clear();
                    boolean isOpenRow = false;
                    if (rowValue != null) {
                        if (!"".equals(rowValue)) {
                            ListItems = ItemCategoryDAL.getListOFPossibleItensOfDatapoit(singleFillingIndicatorAsTableVersion.getTableVID(), Constants.ColumnCoordinate, null, io.getReferenceDate());
                            if(ListItems != null && !ListItems.isEmpty()){
                                listItemsMapped = ListItems.stream().collect(
                                                                 Collectors.groupingBy(
                                                                             DatapointItensDTO::getHeaderCode,
                                                                             TreeMap::new,
                                                                             Collectors.toList()
                                                                         )
                                                                 );
                            }
                            if(rowValue.equals(Constants.OPENROWCODE)){
                                isOpenRow = true;
                            }
                            rowValueAsHeaderCode = HeaderService.getHeaderDTOFromList(headerDTOList,rowValue,Constants.RowCoordinateAsChar,isOpenRow);
                            for (int j = excelPointStruct.getFirstColumn() - 1; j < excelPointStruct.getLastColumn(); j++) {
                                try {
                                    columnValue = ImportExcelValues.getCellValue(workSheet, excelPointStruct.getColumnLabelsRow() - 1, j);
                                } catch (Exception e){
                                    //Erro nº da coluna

                                    errorMsgPerTables.add("Erro na obtencao da coluna, na linha " + rowValue + ".");
                                    
                                    continue;
                                }

                                if ("".equals(columnValue)) {
                                    //"Coluna vazia"
                                    break;
                                };
                                columnValueAsHeaderCode = HeaderService.getHeaderDTOFromList(headerDTOList,columnValue,Constants.ColumnCoordinateAsChar,true);
                            
                                try {
                                    value = ImportExcelValues.getCellValue(workSheet, i, j);
                                    valueEdited = value;
                                } catch (Exception e) {
                                    //Erro a obter valor
                                    errorMsgPerTables.add("Erro na obtencao do valor, na linha " + rowValue + " e na coluna " + columnValue + ".");
                                    continue;
                                }

                                List<ItemCategory> cellListCategory = null;
                                //valid value
                                //Validate if the value is the correcto Type
                                //Apply Imported Rules here
                                CellVariableDTO aux = CellVariableService.getCellVariableServiceDTOFromList(cellVariablesList, rowValueAsHeaderCode, columnValueAsHeaderCode, sheetValueAsHeaderCode,isOpenRow);
                                int cellType = 0;
                                if(aux != null){
                                    cellType = PropertyDAL.getListOfCellVariable(aux.getVariableVid());                              
                                }
                                if (value != null) {
                                    if (Utils.isNumericWithComma(value) && typeListToExclude.contains(cellType)) {
                                        if (value.contains(",")) {
                                            valueEdited = value.replace(",", ".");
                                        }
                                    }
                                    
                                    boolean valid = true;
                                    
                                    if (valueEdited.contains(Constants.LESSTHANOREQUALSCHAR)) {
                                        valueEdited = valueEdited.replaceAll(Constants.LESSTHANOREQUALSCHAR, Constants.LESSTHANOREQUALSSTRING);
                                    }

                                    if (valueEdited.contains(Constants.GREATERTHANOREQUALSCHAR)) {
                                        valueEdited = valueEdited.replaceAll(Constants.GREATERTHANOREQUALSCHAR, Constants.GREATERTHANOREQUALSSTRING);
                                    }

                                    try {
                                        if (cellType == Constants.DATATYPEMONETARY || cellType == Constants.DATATYPEPERCENTAGE) {
                                            valueEdited = Utils.roundToZero(value);
                                        }
                                    } catch (Exception e) {
                                        errorMsgPerTables.add("Erro a normalizar valor numérico, na linha " + rowValue + " e na coluna " + columnValue + ".");
                                        valid = false;
                                    }
                                    
                                     if (cellType == Constants.DATATYPEDATE && listOfIDImportRules.contains(Constants.IMPORTRULEDATE)) {
                                        valueEdited = Utils.dateTreatment(value);
                                        if (valueEdited == null) {
                                            errorMsgPerTables.add("Erro a normalizar valor de data, na linha " + rowValue + " e na coluna " + columnValue + ".");
                                            valid = false;
                                        }
                                    }

                                    if (cellType == Constants.DATATYPEDATETIME && listOfIDImportRules.contains(Constants.IMPORTRULEDATETIME)) {
                                        valueEdited = Utils.dateTimeTreatment(value);
                                        if (valueEdited == null) {
                                            errorMsgPerTables.add("Erro a normalizar valor temporal, na linha " + rowValue + " e na coluna " + columnValue + ".");
                                            valid = false;
                                        }
                                    }
                                    
                                    if ((cellType == Constants.DATATYPEBOOLEAN || cellType == Constants.DATATYPETRUE) && listOfIDImportRules.contains(Constants.IMPORTRULEBOOLEAN)) {
                                        valueEdited = Utils.booleanTreatment(value, cellType);
                                        if (valueEdited == null) {
                                            errorMsgPerTables.add("Erro a normalizar valor booleano, na linha " + rowValue + " e na coluna " + columnValue + ".");                                    
                                            valid = false;
                                        }
                                    }
                                    if(valid){
                                        valid = validateValue(valueEdited,cellType);
                                        if(!valid){
                                            errorMsgPerTables.add("Erro, valor com tipo de dados incorreto na linha " + rowValue + " e na coluna " + columnValue + ".");
                                        }
                                    }
                                    
                                    if (cellType == Constants.DATATYPEENUMERATION || (cellType == Constants.NOTAPPLICABLE && isOpenRow)) {
                                        String valueTemp = valueEdited;
                                        //Error
                                        if (!listItemsMapped.get(columnValue).isEmpty()) {
                                            List<DatapointItensDTO> auxDatapointDTOListOfColumn = new ArrayList<>();
                                            
                                            if (listItemsMapped.get(columnValue).get(0).getValueCode() != null) {
                                                auxDatapointDTOListOfColumn = listItemsMapped.get(columnValue)
                                                        .stream().filter(p -> (p.getValueCode() != null && p.getValueCode().trim().equalsIgnoreCase(valueTemp))
                                                        || (p.getSignature() != null && p.getSignature().trim().equalsIgnoreCase(Constants.EBASEPARATOR + valueTemp))
                                                        || (p.getSignature() != null && p.getSignature().trim().equalsIgnoreCase(valueTemp))
                                                        || (p.getName() != null && p.getName().trim().equalsIgnoreCase(valueTemp)))
                                                        .collect(Collectors.toList());
                                            }
                                            
                                            List<DatapointItensDTO> listItemsForRow = null;
                                            if (!isOpenRow) {
                                                if(auxDatapointDTOListOfColumn != null && (auxDatapointDTOListOfColumn.isEmpty() || auxDatapointDTOListOfColumn.get(0).getSignature() == null)){
                                                    //significa que para a coluna, ele nao encontrou valores possiveis e de verificar na row
                                                    listItemsForRow = ItemCategoryDAL.getListOFPossibleItensOfDatapoit(singleFillingIndicatorAsTableVersion.getTableVID(), Constants.RowCoordinate, rowValue, io.getReferenceDate());
                                                    List<DatapointItensDTO> auxDatapointDTOListOfRow = listItemsForRow.stream().filter(p -> (p.getValueCode() != null && p.getValueCode().trim().equalsIgnoreCase(valueTemp))
                                                            || (p.getSignature() != null && p.getSignature().trim().equalsIgnoreCase(Constants.EBASEPARATOR + valueTemp))
                                                            || (p.getSignature() != null && p.getSignature().trim().equalsIgnoreCase(valueTemp))
                                                            || (p.getName() != null && p.getName().trim().equalsIgnoreCase(valueTemp)))
                                                            .collect(Collectors.toList());
                                                    if(auxDatapointDTOListOfRow != null && !auxDatapointDTOListOfRow.isEmpty() && auxDatapointDTOListOfRow.get(0).getSignature()!= null){
                                                        auxDatapointDTOListOfColumn = auxDatapointDTOListOfRow;
                                                    }
                                                } 
                                            }
                                            if (auxDatapointDTOListOfColumn != null && !auxDatapointDTOListOfColumn.isEmpty() && auxDatapointDTOListOfColumn.get(0).getSignature() != null) {
                                                valueEdited = auxDatapointDTOListOfColumn.get(0).getSignature();
                                            } else if (!isOpenRow && cellType != Constants.NOTAPPLICABLE) {
                                                errorMsgPerTables.add("Erro de valor enumerado desconhecido, na linha " + rowValue + " e na coluna " + columnValue + ".");
                                                valid = false;
                                                //Erro de valor inválido.
                                            }
                                        }
                                    }

                                    if (!valid) {   
                                        
                                        continue;
                                    }
                                }
                                
                                
                                if(aux == null|| aux.getVariableVid() == 0 || value == null){
                                    //Skip (Grey Areas in the report)
                                    if(isOpenRow && value != null){
                                        //BuildRowKey
                                        if(rowKeyImportKey == null){
                                            rowKeyImportKey = new InImportKey();
                                            //rowKeyImportKey.setKeyType(em.getReference(InKeyType.class,Constants.ROWKEYTYPE));
                                            InKeyType keyType = cm.em.getReference(InKeyType.class,Constants.ROWKEYTYPE);
                                            rowKeyImportKey.setKeyType(keyType);

                                            rowKeyImportKey.setListPropertyValues(new ArrayList<>());
                                            Connection.persist(cm, rowKeyImportKey);
                                        }
                                        
                                        InKeyAssociation desagregationCodeAssociation = new InKeyAssociation();
                                        desagregationCodeAssociation.setImportedKey(rowKeyImportKey);
                                        desagregationCodeAssociation.setPropertyName(listItemsMapped.get(columnValue).get(Constants.FIRSTRESULT).getXBRLHeader());
                                        desagregationCodeAssociation.setPropertyValue((Utils.isNumericWithComma(value)? (value.contains(",") ? value.replace(",", ".") : value) : valueEdited));
                                        desagregationCodeAssociation.setPropertyOriginalValue(value);

                                        rowKeyImportKey.getListPropertyValues().add(desagregationCodeAssociation);
                                        /*em.persist(desagregationCodeAssociation);
                                        em.merge(rowKeyImportKey);*/
                                        Connection.persist(cm, desagregationCodeAssociation);
                                        Connection.merge(cm, rowKeyImportKey);

                                        
                                    } 
                                    continue;
                                }
                                if(cellListCategory != null && cellListCategory.size() > 1 && !Utils.isNumericWithComma(value) && cellType == Constants.DATATYPEENUMERATION){
                                    valueEdited = itemCategoryCellLastFilter(value,cellListCategory,aux.getVariableVid());
                                }
                                
                                if(typeListToExclude.contains(cellType) &&Utils.isNumericWithComma(value)) {
                                    if (value.contains(",")) {
                                        valueEdited = value.replace(",", ".");
                                    }else{
                                        valueEdited = value;
                                    }
                                }
                                
                                if(notInsertedInImportedValuesPerSheet){
                                    notInsertedInImportedValuesPerSheet=false;
                                    Connection.persist(cm, importedTableTemp);
                                    
                                }
                                InImportedValuesTemp newValue = new InImportedValuesTemp();
                                newValue.setImportedTableId(importedTableTemp);
                                newValue.setImportedValue(value);
                                newValue.setRuleValue(valueEdited);
                                if (!cm.isOpen()){
                                    cm.reset();
                                }
                                newValue.setVariableVersion(cm.em.getReference(VariableVersion.class, aux.getVariableVid()));
                                newValue.setCell(cm.em.getReference(Cell.class, aux.getCellId()));
                                newValue.setColuna(columnValueAsHeaderCode == null ? null : columnValueAsHeaderCode.getCode());
                                newValue.setLinha(rowValueAsHeaderCode == null ? null : rowValueAsHeaderCode.getCode());
                                newValue.setSheet(sheetValueAsHeaderCode == null ? null : sheetValueAsHeaderCode.getCode());  
                                nRegistos++;                                
                                hasInsertedValue = true;                               
                                if(!isOpenRow){
                                    Connection.persist(cm, newValue);
                                }else{
                                    openRowAuxListForPersiste.add(newValue);
                                }
                            }
                        }
                    }
                    long countRowKeys = properties.values().stream().filter(propertyType -> propertyType == Constants.ROWKEYTYPE).count();
                    if(isOpenRow && (rowKeyImportKey == null || (rowKeyImportKey.getListPropertyValues() != null && rowKeyImportKey.getListPropertyValues().size() != countRowKeys)) && !openRowAuxListForPersiste.isEmpty()){

                    } else {
                    //Loop to Set Rowkey
                        for(InImportedValuesTemp importedValue:openRowAuxListForPersiste){
                            String rowKeyToValue = null;
                            if(rowKeyImportKey.getListPropertyValues() != null && !rowKeyImportKey.getListPropertyValues().isEmpty()){
                                rowKeyToValue = rowKeyImportKey.getListPropertyValues().stream().map(InKeyAssociation::getPropertyValue).collect(Collectors.joining("|"));                                                        
                            }
                            importedValue.setRowKey(rowKeyToValue);
                            importedValue.setImportKey(rowKeyImportKey);
                            Connection.persist(cm, importedValue);
                        }
                    }

                    if (nRegistos % 1000 == 0) {
                        if (!cm.em.getTransaction().isActive()) {
                            cm.em.getTransaction().begin();
                        }
                        cm.em.flush();
                        cm.em.clear();
                    }
                }
                
                importedTableTemp.setEndTimestamp(LocalDateTime.now());
                
                if(importedTableTemp.getIoState().getIoStateId() == Constants.processoOkEmpty){
                    if (errorMsgPerTables.isEmpty() && hasInsertedValue) {
                        importedTableTemp.setIoState(Info.getInstance().getIOStateByID(Constants.processoOk));
                        hasOk = true;
                    } else if (!errorMsgPerTables.isEmpty() && !hasInsertedValue) {
                        hasEmpty = true;
                    } else if (!errorMsgPerTables.isEmpty()) {
                        importedTableTemp.setIoState(Info.getInstance().getIOStateByID(Constants.processoNotOk));//new IOState(Constants.processoNotOk, new IOTypeState(Constants.tipoStateNotOk)));

                        hasNotOk = true;

                    } else if (errorMsgPerTables.isEmpty() && !hasInsertedValue) {
                        hasEmpty = true;
                    }
                } 
                
                Connection.merge(cm, importedTableTemp);
                       
                if (!cm.em.getTransaction().isActive()) {
                    cm.em.getTransaction().begin();
                }
                cm.em.flush();
                cm.em.clear();
                
                progressService.setImportProgress(completedTables++, Integer.valueOf(totalNumberSheets) + 1);

            }
            workBook.close();
        }catch (Exception e) {
            e.printStackTrace();
            ioState = new IOState(Constants.processoNotOk, new IOTypeState(Constants.tipoStateNotOk));
            LOG.error("Error With Import Process:" + e.getMessage());

            return ioState;
        }finally{
            if(em != null){
                if(em.isOpen()){
                    em.close();
                }
            }
        }
        
        if (hasNotOk && !hasOk) {
            ioState = Info.getInstance().getIOStateByID(Constants.processoNotOk);//new IOState(Constants.processoNotOk, new IOTypeState(Constants.tipoStateNotOk));
        } else if (hasNotOk && hasOk) {
            ioState = Info.getInstance().getIOStateByID(Constants.processoOkWithError);//new IOState(Constants.processoOkWithError, new IOTypeState(Constants.tipoStateOK));
        } else if (hasEmpty) {
            ioState = Info.getInstance().getIOStateByID(Constants.processoOkEmpty);//new IOState(Constants.processoOkEmpty, new IOTypeState(Constants.tipoStateOK));
        } else {
            ioState = Info.getInstance().getIOStateByID(Constants.processoOk);//new IOState(Constants.processoOk, new IOTypeState(Constants.tipoStateOK));
        }
        
        //ioState = new IOState(Constants.processoOk, new IOTypeState(Constants.tipoStateOK));
        return ioState;
        
    }
    
    // main method that is being called
    public void run() {
        setIsActive(true);
        ConnectionManager cm = new ConnectionManager(Connection.getEm());
        List<Integer> listOfIDImportRules = listOfImportRulesToApply != null ? listOfImportRulesToApply : new ArrayList<>();
        //Add to the FileHistory
        if(domain.length() > 3){
            domain = domain.substring(0,3).toUpperCase();
        }                     
        IO io = null;
        IOState iostate = Info.getInstance().getIOStateByID(Constants.processoPending);//new IOState(Constants.processoPending, new IOTypeState(Constants.tipoStatePending));
        ConfAction action = Info.getInstance().getConfActionByID(Integer.valueOf(Constants.actionImport));                       
        try {
            io = new IO(iostate, referenceDate, moduleVersion, domain.toUpperCase(), entity, 
                    LocalDateTime.now(), null, action, "abc", filename, Constants.IMPORT+alterFilename, alterFilename);
            
            Connection.persist(cm, io);
            //IODAL.persist(io);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        IOState result = null;       
        
        result = mainImport(io,listOfIDImportRules);
        
        if(result != null){
            io.setIoState(result);
            io.setEndTimestamp(LocalDateTime.now());
            //Connection.merge(io);
            Connection.merge(cm, io);
        }
        progressService.setImportProgress(1,1);
        Validator_2_0 validationAction = new Validator_2_0(moduleVersion, referenceDate, entity, domain,progressService);

        validationAction.startValidation(referenceDate, moduleVersion, domain.toUpperCase(), entity, filename, io);
    }

    /**
     * method used to build the desagregation code (code that some sheets have), it includes the value 
     * @param rawDesagregationCode RAW desagregationCodeAssociation from the Excel
     * @param desagregationCodes List with the desagregation Codes
     * @return the InImportKey that references all DesagregationCodes
     */
    public InImportKey buildDesagregationCode(String rawDesagregationCode, int tableVID, int tableID, LocalDate referenceDate, ConnectionManager cm){
       EntityManager em = null;
       InImportKey desagregationImportKey = null;
       try {
        em = Connection.getEm();
        List<DatapointItensDTO> ListItems = ItemCategoryDAL.getListOFPossibleItensOfDatapoit(tableVID, Constants.SheetCoordinate, null, referenceDate);
        if(ListItems.isEmpty()){
            //Don't Have desagregationCode
            if(rawDesagregationCode != null && !rawDesagregationCode.trim().equals("") && !Utils.isNumeric(rawDesagregationCode)){
            }
            return null;
        }
        TreeMap <String, List<DatapointItensDTO>> listItemsMapped = ListItems.stream().collect(
                                                                 Collectors.groupingBy(
                                                                             DatapointItensDTO::getHeaderCode,
                                                                             TreeMap::new,
                                                                             Collectors.toList()
                                                                         )
                                                                 );
        String currentKey  = listItemsMapped.firstKey();
        String[] ExcelDesagregationCodeList = rawDesagregationCode.split("\\|");
        boolean firstLoop = true;
        for(String stringSplittedDesagregation : ExcelDesagregationCodeList){
            List<DatapointItensDTO> possibleCurrentValues =  listItemsMapped.get(currentKey);
            currentKey  = listItemsMapped.higherKey(currentKey);
            if(firstLoop){
                firstLoop = false;
                desagregationImportKey = new InImportKey();
                desagregationImportKey.setKeyType(cm.em.getReference(InKeyType.class,Constants.DESAGREGATIONCODETYPE));
                desagregationImportKey.setListPropertyValues(new ArrayList<>());
                cm.em.persist(desagregationImportKey);
            }
            if(!Utils.isNumeric(stringSplittedDesagregation)){
                //field to Ignore
                
                if(!possibleCurrentValues.isEmpty()){
                    InKeyAssociation desagregationCodeAssociation = new InKeyAssociation();
                    desagregationCodeAssociation.setImportedKey(desagregationImportKey);
                    desagregationCodeAssociation.setPropertyName(possibleCurrentValues.get(Constants.FIRSTRESULT).getXBRLHeader());
                    if(possibleCurrentValues.get(Constants.FIRSTRESULT).getSignature() != null){
                        //Validate Against the ListList<Person> filteredPeople = people.stream()
                         possibleCurrentValues = possibleCurrentValues.stream().filter(p -> (p.getValueCode() != null && p.getValueCode().trim().equalsIgnoreCase(stringSplittedDesagregation))
                            || (p.getSignature() != null && p.getSignature().trim().equalsIgnoreCase(Constants.EBASEPARATOR + stringSplittedDesagregation))
                            || (p.getSignature() != null && p.getSignature().trim().equalsIgnoreCase(stringSplittedDesagregation))
                            || (p.getName() != null && p.getName().trim().equalsIgnoreCase(stringSplittedDesagregation)))
                            .collect(Collectors.toList());
                        if(possibleCurrentValues.size() == Constants.UNIQUEELEMENTONLIST){
                            desagregationCodeAssociation.setPropertyValue(possibleCurrentValues.get(Constants.FIRSTRESULT).getSignature());
                        }else{
                        }
                    }else{
                        desagregationCodeAssociation.setPropertyValue(stringSplittedDesagregation);
                    }
                    desagregationImportKey.getListPropertyValues().add(desagregationCodeAssociation);
                    cm.em.persist(desagregationCodeAssociation);
                    cm.em.persist(desagregationImportKey);
                 }
            } else {
                InKeyAssociation desagregationCodeAssociation = new InKeyAssociation();
                    desagregationCodeAssociation.setImportedKey(desagregationImportKey);
                    desagregationCodeAssociation.setPropertyName(Constants.SHEETCODE);
                    desagregationCodeAssociation.setPropertyValue(stringSplittedDesagregation);
                    desagregationImportKey.getListPropertyValues().add(desagregationCodeAssociation);
                    cm.em.persist(desagregationCodeAssociation);
                    cm.em.persist(desagregationImportKey);
                    
            }

        }
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(err -> System.out.println(err.toString()));
        } catch(Exception e){
            e.printStackTrace();
            return null;
        } /*finally{
            Connection.close(em);
        }*/
        return desagregationImportKey;
    }
    
    /**
     * method used to check if the thread has been interrupted
     * @param em
     * @return 
     */
    public boolean checkIfThredIsCanceled(EntityManager em){
        if (Thread.interrupted()) {
                    if(em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                        //em.close();
                    }
                    return true;
        }
        return false;
    }
        
    public String itemCategoryCellLastFilter(String value, List<ItemCategory> cellListCategory, Integer variableVID){
        Set<Integer> itemIDs = ItemCategoryDAL.getListItemCategoryBasedOnTableVariableVID(variableVID);
        cellListCategory= cellListCategory.stream()
            .filter(p -> itemIDs.contains(p.getItem().getItemId()))
            .collect(Collectors.toList());
        if(cellListCategory.size() == 1){
            return cellListCategory.get(1).getSignature();
        }
        //IF ENTER HERE
        if(itemIDs.size() == 0){
            return value;
        }
        return null;
    }
    
    public String itemCategoryDesagregationLastFilter(String value,List<ItemCategory> cellListCategory,InImportedTablesTemp importedTable){
        List<ItemCategory> aux = cellListCategory;
        Set<Integer> itemIDs = ItemCategoryDAL.getSetIDsItemCategoryOfTableDesagregationCode(importedTable,Constants.ColumnCoordinate);
        cellListCategory= cellListCategory.stream()
            .filter(p -> itemIDs.contains(p.getItem().getItemId()))
            .collect(Collectors.toList());
        if(cellListCategory.size() == 1){
            return cellListCategory.get(1).getSignature();
        }
        if(itemIDs.size() == 0){
            return value;
        }
        return null;
    }
    
    public boolean validateValue(String value, int cellType) {
        switch (cellType) {
            case Constants.DATATYPEDECIMAL:
            case Constants.DATATYPEMONETARY:
            case Constants.DATATYPEPERCENTAGE:
                if (Utils.isNumeric(value)) {
                    return true;
                }
                break;
            case Constants.DATATYPEINTEGER:
                if (Utils.isNumeric(value) && !value.contains(".")) {
                    return true;
                }
                break;
            case Constants.DATATYPEBOOLEAN:
                return Utils.validateFromARegex(value, Constants.BOOLEANPATTERN);
            case Constants.DATATYPETRUE:
                return Utils.validateFromARegex(value, Constants.TRUEPATTERN);
            case Constants.DATATYPEDATETIME:
                return Utils.validateFromARegex(value, Constants.DATETIMEPATTERN);
            case Constants.DATATYPEDATE:
                return Utils.validateFromARegex(value, Constants.DATEPATTERN);
            case Constants.DATATYPEURI:
                return Utils.validateFromARegex(value, Constants.URIPATTERN);
            case Constants.DATATYPEORDINALS:
                //Validate ORDINALS (No Ideia what it is)
                break;
            case Constants.DATATYPESTRINGINCLUDINGEMPTY:
            case Constants.DATATYPEENUMERATION: //Enumeration Validates Later to check if exists in List
                return true;
            case Constants.DATATYPESTRINGNONEMPTY:
                if (value != null && value != "") {
                    return true;
                }
            case Constants.DATATYPENOTAPPLICABLE:
                return true;
        }
        return false;
    }
    
}
