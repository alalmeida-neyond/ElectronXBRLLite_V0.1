package com.example.demo.controller.Objects.Operations.Where;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.demo.controller.Objects.Validation.ValKey;
import com.example.demo.controller.Objects.Validation.ValResult;

public class WhereEqualsToStrategy implements WhereStrategy {
    
    private final static Logger LOG = Logger.getLogger(WhereEqualsToStrategy.class.getName());
    
    @Override
    public boolean filterResultsUsingWhere(ValResult result, List<Map.Entry<String, String>> allConditions/*, boolean isChildOfFilter*/) {
        Map<String, String> propertiesValues = new HashMap<>();
        ValKey keyFromResult = new ValKey();
        boolean respectFilter = true;
        
        try {
            if (result != null && result.getKey() != null && !result.getKey().hasKeysPropertiesIndexsNull()) {
                keyFromResult = result.getKey();
                if(keyFromResult.getDpmKeys() != null && !keyFromResult.getDpmKeys().isEmpty()){
                    propertiesValues = keyFromResult.getDpmKeys();
                }
                
                if(propertiesValues != null && !propertiesValues.isEmpty()){
                    for(Map.Entry<String, String> condition : allConditions){
                        String propertyValue = propertiesValues.get(condition.getKey());
                        if (propertyValue == null || !propertyValue.equals(condition.getValue())) {
                            respectFilter = false;
                            break;
                        }
                        
                        /*if(isChildOfFilter){
                            result.getKey().getDpmKeys().remove(condition.getKey());
                        }*/
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
