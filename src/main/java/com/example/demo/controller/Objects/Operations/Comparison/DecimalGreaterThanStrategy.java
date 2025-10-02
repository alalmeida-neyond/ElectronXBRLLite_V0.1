package com.example.demo.controller.Objects.Operations.Comparison;

import java.math.BigDecimal;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;

public class DecimalGreaterThanStrategy implements ComparisonStrategy {

    @Override
    public ValResult compare(Object left, Object right, boolean useIntervals, BigDecimal leftMargin, BigDecimal rightMargin) {
        Boolean result = null;
        boolean usedMargin = false;
        BigDecimal difference = BigDecimal.ZERO;
        
        if (useIntervals) {
            if (left != null && right != null) {
                BigDecimal centreLeft = ((BigDecimal) left);
                BigDecimal centreRightWithRadiusAdd = ((BigDecimal) right).subtract(((BigDecimal) leftMargin).add((BigDecimal) rightMargin));
                result = centreLeft.compareTo(centreRightWithRadiusAdd) > 0;
                usedMargin = true;
            }
        } else {
            if (left != null && right != null) {
                result = ((BigDecimal) left).compareTo((BigDecimal) right) > 0;
            }
        }    
        
        if (result != null) {
            if (!result) { 
                difference = ((BigDecimal) left).subtract((BigDecimal) right).abs(); 
                usedMargin = false; 
            } else {
                if (usedMargin) {
                    difference = ((BigDecimal) left).subtract((BigDecimal) right).abs(); 
                } else {
                    usedMargin = false;
                    difference = null; 
                }
            }
        } else {
            usedMargin = false;
        }
        
        ValValue valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEBOOLEAN), result != null ? result.toString() : null);
        ValResult valResult = new ValResult(valueParentResult);
        
        valResult.setUsedMargin(usedMargin);
        if(difference != null){
            valResult.setDifference(difference); 
        }
        
        return valResult;
    }
}
