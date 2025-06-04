/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.demo.controller.Objects.Where;

import java.util.List;
import java.util.Map;

import com.example.demo.controller.Objects.Validation.ValResult;

public interface WhereStrategy {
    
     public boolean filterResultsUsingWhere(ValResult result, List<Map.Entry<String, String>> allConditions);
}
