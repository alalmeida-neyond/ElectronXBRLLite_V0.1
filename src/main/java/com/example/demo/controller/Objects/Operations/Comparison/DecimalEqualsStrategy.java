package com.example.demo.controller.Objects.Operations.Comparison;

import java.math.BigDecimal;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;


public class DecimalEqualsStrategy implements ComparisonStrategy{

    @Override
    public ValResult compare(Object left, Object right, boolean useIntervals, BigDecimal leftMargin, BigDecimal rightMargin) {
        Boolean result = null;
        boolean usedMargin = false;
        BigDecimal difference = BigDecimal.ZERO;
        
        if (useIntervals) {
            if (left != null && right != null) {
                BigDecimal centreDiff = ((BigDecimal) left).subtract((BigDecimal) right).abs();
                BigDecimal totalRadius = ((BigDecimal) leftMargin).add((BigDecimal) rightMargin);
                result = centreDiff.compareTo(totalRadius) <= 0;
                usedMargin = true;
            }
        } else {
            if (left != null && right != null) {
                result = ((BigDecimal) left).compareTo((BigDecimal) right) == 0;
            }
        }
        
        if (result != null) {
            if (!result) { 
                if (left != null && right != null) {
                    difference = ((BigDecimal) left).subtract((BigDecimal) right).abs(); 
                    usedMargin = false; 
                }
            } else {
                if (usedMargin) {
                    if (left != null && right != null) {
                        difference = ((BigDecimal) left).subtract((BigDecimal) right).abs(); 
                    }
                } else {
                    usedMargin = false;
                    difference = null; 
                }
            }
        } else {
            usedMargin = false;
        }
        
        ValValue valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), result != null ? result.toString() : null);
        ValResult valResult = new ValResult(valueParentResult);
        
        valResult.setUsedMargin(usedMargin);
        if(difference != null){
            valResult.setDifference(difference); 
        }
        
        return valResult;
    }
}
