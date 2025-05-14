/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Aggregation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.demo.controller.Objects.*;


public class AggregationMaxStrategy implements AggregationStrategy {

    @Override
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped) {
        boolean isToUseIntervals = false;
        BigDecimal fixMargin = BigDecimal.ZERO;
        BigDecimal valueMargin = BigDecimal.ZERO;
        BigDecimal maxResult = null; 
        BigDecimal marginResult = BigDecimal.ZERO; 
        ValValue valueParentResult = new ValValue();

        try {
            if (operandChild != null) {
                //FIX ME to use stream.reduce
                for (ValResult result : resultsGrouped) {
                    ValResult resultTemp = (result == null || result.getRawValue() == null)? OperationsUtils.applyDefaultValue(operandChild, result, result != null ? result.getDomain() : null) : result;
                    BigDecimal value = (resultTemp == null || resultTemp.getRawValue() == null) ? null : new BigDecimal(resultTemp.getRawValue());
                    valueMargin = BigDecimal.ZERO;
                    
                    isToUseIntervals = OperationsUtils.isToUseMargin(operandChild, resultTemp);
                    if(isToUseIntervals){
                        valueMargin = OperationsUtils.setMarginValue(operandChild, result);
                    }
                    
                    if (value != null) {
                        if (maxResult == null || value.compareTo(maxResult) > 0) {
                            maxResult = value;
                            marginResult = valueMargin;
                        }
                    }
                }

                if(maxResult != null){
                    valueParentResult.setValue(maxResult.toPlainString());
                }
                
                valueParentResult.setDatatype(OperationsUtils.getDataTypeByID(Constants.DECIMAL));
                return new ValResult(valueParentResult, marginResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
