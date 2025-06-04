/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.demo.controller.Objects.NumericAggregationStrategy;

import java.util.List;
import java.util.Map;

import com.example.demo.controller.Objects.*;
import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;

public interface NumericAggregationStrategy {
    
    public ValResult evaluate(ValNode parent, List<Map.Entry<ValNode, ValResult>> resultsGrouped);
    
}
