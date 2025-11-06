package com.example.demo.controller.Objects.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.demo.DTOs.DatapointItensDTO;
import com.example.demo.Data.Connection;
import com.example.demo.Data.ConnectionManager;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.DAL.ItemCategoryDAL;
import com.example.demo.controller.Objects.Entities.DAL.TableVersionDAL;
import com.example.demo.controller.Objects.IO.IO;

public class DesagregationCodeBuilder {
    
    private final Map<Integer, Integer> desagregationCodeTypeOfMaps;
    private final Map<Integer, List<Map.Entry<String, Integer>>> dataTypeOfDesagCodesOfMaps;
    private final Map<Integer, Map<String, List<DatapointItensDTO>>> possibleValuesForDesagCodesEnumerated;
    
    private ConnectionManager cm;
    
    private final String importType;
    private String sheetName;
    private List<String> errorMessages;
    
    public DesagregationCodeBuilder(IO io, ConnectionManager cm, String importType) {
        desagregationCodeTypeOfMaps = TableVersionDAL.getDesagregationCodeTypeOfMaps(io.getModule());
        dataTypeOfDesagCodesOfMaps = ItemCategoryDAL.getDataTypeOfDesagCodesOfMaps(io.getModule(), io.getReferenceDate());
        possibleValuesForDesagCodesEnumerated = ItemCategoryDAL.getPossibleValuesForDesagCodes(io.getModule(), io.getReferenceDate());   
        
        this.importType = importType;
        this.cm = cm;
        errorMessages = new ArrayList<>();
    }
    
    public boolean isOpenSheets(Integer tableVID){
        return desagregationCodeTypeOfMaps.containsKey(tableVID);
    }
    
    public InImportKey buildDesagCodeKey(Integer tableVID, String desagregationCodeRaw, String sheetName) {
         if(desagregationCodeRaw == null || desagregationCodeRaw.isEmpty()){
            errorMessages.add(Constants.MESSAGEERROREXPECTEDDESAGCODE(sheetName));
            return null;
        }
        this.sheetName = sheetName;
        Integer desagCodeType = desagregationCodeTypeOfMaps.get(tableVID);
        InImportKey desagCodeKey = null;
        
        if(desagCodeType == Constants.DESAGREGATIONCODEFIXEDTYPE){
            desagCodeKey = buildDesagCodeFixed(tableVID, desagregationCodeRaw);
        } else if (desagCodeType == Constants.DESAGREGATIONCODETYPE) {
            desagCodeKey = buildDesagCodeNormal(tableVID, desagregationCodeRaw);
        }
                
        this.sheetName = null;
        return desagCodeKey;
    }
    
    public InImportKey buildDesagCodeFixed(Integer tableVID, String desagregationCodeRaw){
        DatapointItensDTO possibleValue = validatePossibleValueInsertIntoDesagCode(tableVID, Constants.SHEETCODE, desagregationCodeRaw, true);
        if(possibleValue == null){
            if(importType.equalsIgnoreCase(Constants.IMPORTNORMAL))
                errorMessages.add(Constants.MESSAGEERRORDESAGCODE(this.sheetName));
            return null;
        }
        
        InImportKey desagCodeFixedKey = new InImportKey(cm.em.getReference(InKeyType.class, Constants.DESAGREGATIONCODEFIXEDTYPE));
        Connection.persist(cm, desagCodeFixedKey);
        
        InKeyAssociation desagregationCodeAssociation = new InKeyAssociation(possibleValue.getXBRLHeader(), possibleValue.getHeaderCode(), possibleValue.getHeaderCode(), desagCodeFixedKey);
        Connection.persist(cm, desagregationCodeAssociation);
        
        List<InKeyAssociation> keyAssociationList = new ArrayList<>();
        keyAssociationList.add(desagregationCodeAssociation);
        desagCodeFixedKey.setListPropertyValues(keyAssociationList);
        
        return desagCodeFixedKey;
    }
    
    public InImportKey buildDesagCodeNormal(Integer tableVID, String desagregationCodeRaw){
        List<Map.Entry<String, Integer>> dataTypeOfDesagCodesOfThisMap = dataTypeOfDesagCodesOfMaps.get(tableVID);
        String[] desagCodesSplited = desagregationCodeRaw.split("\\|");
        
        if(desagCodesSplited.length != dataTypeOfDesagCodesOfThisMap.size()){
            if(importType.equalsIgnoreCase(Constants.IMPORTNORMAL))
                errorMessages.add(Constants.MESSAGEERRORINCOMPLETEDESAGCODE(this.sheetName));
            return null;
        }
        
        List<InKeyAssociation> keysAssociation = new ArrayList<>();
        for(int i = 0; i< desagCodesSplited.length; i++){
            String partOfDesagCode = desagCodesSplited[i];
            Map.Entry<String, Integer> propertyWithDatatype = dataTypeOfDesagCodesOfThisMap.get(i);
            
            if(propertyWithDatatype.getValue() == Constants.DATATYPEENUMERATION){
                DatapointItensDTO valueOfDesagCode = validatePossibleValueInsertIntoDesagCode(tableVID, propertyWithDatatype.getKey(), partOfDesagCode, false);
                if(valueOfDesagCode == null){
                    if(importType.equalsIgnoreCase(Constants.IMPORTNORMAL)) 
                        errorMessages.add(Constants.MESSAGEERRORDESAGCODE(this.sheetName));
                    return null;
                }
                
                InKeyAssociation desagregationCodeAssociation = new InKeyAssociation(valueOfDesagCode.getXBRLHeader(), valueOfDesagCode.getSignature(), partOfDesagCode);
                keysAssociation.add(desagregationCodeAssociation);
            } else if (propertyWithDatatype.getValue() == Constants.DATATYPESTRINGNONEMPTY){
                InKeyAssociation desagregationCodeAssociation = new InKeyAssociation(propertyWithDatatype.getKey(), partOfDesagCode, partOfDesagCode);
                keysAssociation.add(desagregationCodeAssociation);
            } else {
                if(importType.equalsIgnoreCase(Constants.IMPORTNORMAL))
                    errorMessages.add(Constants.MESSAGEERRORUNKOWNDATATYPEDESAGCODE(this.sheetName));
                return null;
            }
        }
        
        InImportKey desagCodeKey = new InImportKey(cm.em.getReference(InKeyType.class, Constants.DESAGREGATIONCODETYPE));
        Connection.persist(cm, desagCodeKey);
        for(InKeyAssociation keyAssociation : keysAssociation){
            keyAssociation.setImportedKey(desagCodeKey);
            Connection.persist(cm, keyAssociation);
        }
        
        desagCodeKey.setListPropertyValues(keysAssociation);
        return desagCodeKey;
    }
    
    private DatapointItensDTO validatePossibleValueInsertIntoDesagCode(Integer tableVID, String property, String partOfDesagCode, boolean isDesagCodeFixed){
        Map<String, List<DatapointItensDTO>> possibleValuesMap = possibleValuesForDesagCodesEnumerated.get(tableVID);
        if (possibleValuesMap == null || possibleValuesMap.isEmpty()){
            return null;
        }
        
        List<DatapointItensDTO> possibleValuesForProperty = possibleValuesMap.get(property);
        if (possibleValuesForProperty == null || possibleValuesForProperty.isEmpty()){
            return null;
        }
                
        for(DatapointItensDTO possibleValue : possibleValuesForProperty){
            if(possibleValue.matchesWithThisItem(partOfDesagCode, isDesagCodeFixed)) return possibleValue;           
        }
                
        return null;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
    
    public void clearErrorMessages() {
        errorMessages.clear();
    }
}