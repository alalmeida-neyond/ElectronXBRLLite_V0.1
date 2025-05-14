/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Aggregation;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.controller.Objects.*;

/**
 *
 * @author njesus
 */
public class AggregationCountStrategy implements AggregationStrategy {

    @Override
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped) {
        BigDecimal countResult = BigDecimal.ZERO;
        ValValue valueParentResult = new ValValue();
        
        try {
            if(operandChild != null){
                for(ValResult result : resultsGrouped){
                    ValResult resultTemp = (result == null || result.getRawValue() == null)? OperationsUtils.applyDefaultValue(operandChild, result, result != null ? result.getDomain() : null) : result;
                    BigDecimal value = (resultTemp == null || resultTemp.getRawValue() == null) ? null : new BigDecimal(resultTemp.getRawValue());
                    if(value != null){
                        countResult = countResult.add(BigDecimal.ONE);
                    }
                }
                
                if(countResult != null){
                    valueParentResult.setValue(countResult.toPlainString());
                }
                
                valueParentResult.setDatatype(OperationsUtils.getDataTypeByID(Constants.DECIMAL));
                return new ValResult(valueParentResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
