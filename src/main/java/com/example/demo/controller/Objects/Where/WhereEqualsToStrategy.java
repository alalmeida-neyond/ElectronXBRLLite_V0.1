/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Where;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.controller.Objects.*;

public class WhereEqualsToStrategy implements WhereStrategy {

    @Override
    public boolean filterResultsUsingWhere(ValResult result, List<Map.Entry<String, String>> allConditions) {
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
                    }
                    return respectFilter;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            //TODO: INCLUIR LOGS
        }
        return false;
    }
}
