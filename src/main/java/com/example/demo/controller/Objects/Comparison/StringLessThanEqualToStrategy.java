/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Comparison;

import java.math.BigDecimal;

import com.example.demo.controller.Objects.*;

public class StringLessThanEqualToStrategy implements ComparisonStrategy {

    @Override
    public ValResult compare(Object left, Object right, boolean useIntervals, BigDecimal leftMargin, BigDecimal rightMargin) {
        Boolean result = null;
        
        if (left != null && right != null) {
            result = ((String) left).compareTo((String) right) <= 0;
        } 
        
        ValValue valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), result != null ? result.toString() : null);
        ValResult valResult = new ValResult(valueParentResult);
        valResult.setUsedMargin(false);
        return valResult;
    }

}
