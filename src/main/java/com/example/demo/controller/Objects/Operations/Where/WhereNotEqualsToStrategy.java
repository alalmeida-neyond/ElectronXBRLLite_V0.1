package com.example.demo.controller.Objects.Operations.Where;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

import com.example.demo.controller.Objects.Validation.ValKey;
import com.example.demo.controller.Objects.Validation.ValResult;

public class WhereNotEqualsToStrategy implements WhereStrategy {

    @Override
    public boolean filterResultsUsingWhere(ValResult result, List<Map.Entry<String, String>> allConditions) {
        Map<String, String> propertiesValues = new HashMap<>();
        ValKey keyFromResult = new ValKey();
        boolean respectFilter = true;

        final Logger LOG = Logger.getLogger(WhereNotEqualsToStrategy.class);
        
        try {
            if (result != null && result.getKey() != null && !result.getKey().hasKeysPropertiesIndexsNull()) {
                keyFromResult = result.getKey();
                if(keyFromResult.getDpmKeys() != null && !keyFromResult.getDpmKeys().isEmpty()){
                    propertiesValues = keyFromResult.getDpmKeys();
                }
                
                if(propertiesValues != null && !propertiesValues.isEmpty()){
                    for(Map.Entry<String, String> condition : allConditions){
                        String propertyValue = propertiesValues.get(condition.getKey());
                        if (propertyValue == null || propertyValue.equals(condition.getValue())) {
                            respectFilter = false;
                            break;
                        }
                    }
                    return respectFilter;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Erro em filterResultsUsingWhere: " + e.getMessage());
        }
        return false;
    }
    
}
