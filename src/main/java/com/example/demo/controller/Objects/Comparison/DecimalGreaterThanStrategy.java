/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Comparison;

import java.math.BigDecimal;

import com.example.demo.controller.Objects.*;

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
                if (left != null && right != null) {
                    difference = ((BigDecimal) left).subtract((BigDecimal) right).abs(); 
                }
                usedMargin = false; 
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
