/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.demo.controller.Objects.Aggregation;

import java.util.List;

import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;

public interface AggregationStrategy {
    
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped);
}
