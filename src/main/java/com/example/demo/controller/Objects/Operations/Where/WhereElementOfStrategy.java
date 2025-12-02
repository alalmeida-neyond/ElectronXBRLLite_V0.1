package com.example.demo.controller.Objects.Operations.Where;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.demo.controller.Objects.Validation.ValKey;
import com.example.demo.controller.Objects.Validation.ValResult;

public class WhereElementOfStrategy implements WhereStrategy{

    private final static Logger LOG = Logger.getLogger(WhereElementOfStrategy.class.getName());
    
    @Override
    public boolean filterResultsUsingWhere(ValResult result, List<Map.Entry<String, String>> allConditions/*, boolean isChildOfFilter*/) {
        Map<String, String> propertiesValues = new HashMap<>();
        ValKey keyFromResult = new ValKey();
        boolean respectFilter = false;
        
        try {
            if (result != null && result.getKey() != null && !result.getKey().hasKeysPropertiesIndexsNull()) {
                keyFromResult = result.getKey();
                if(keyFromResult.getDpmKeys() != null && !keyFromResult.getDpmKeys().isEmpty()){
                    propertiesValues = keyFromResult.getDpmKeys();
                }
                
                if(propertiesValues != null && !propertiesValues.isEmpty()){
                    for(Map.Entry<String, String> propertyEntry : propertiesValues.entrySet()){
                        String propertyValue = propertyEntry.getValue();
                        
                        if(propertyValue != null){
                            for(Map.Entry<String, String> conditionEntry : allConditions){
                                if(conditionEntry.getKey().equals(propertyEntry.getKey()) && conditionEntry.getValue().equals(propertyValue)){
                                    respectFilter = true;
                                    
                                    break;
                                }
                            }
                        }
                    }
                    return respectFilter;
                }
            }
            return false;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro ao realizar a operação de Where & Equals",e);
        }
        return false;
    }
    
}
