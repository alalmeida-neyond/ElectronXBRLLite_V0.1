package com.example.demo.controller.Objects.Operations.Where;

import java.util.List;
import java.util.Map;

import com.example.demo.controller.Objects.Validation.ValResult;

public interface WhereStrategy {
    
     public boolean filterResultsUsingWhere(ValResult result, List<Map.Entry<String, String>> allConditions);
}
