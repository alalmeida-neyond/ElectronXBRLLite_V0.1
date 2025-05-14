/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Comparison;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.controller.Objects.*;

public class DateTimeEqualsStrategy implements ComparisonStrategy {

    @Override
    public ValResult compare(Object left, Object right, boolean useIntervals, BigDecimal leftMargin, BigDecimal rightMargin) {
        Boolean result = null;
        
        if (left != null && right != null) {
            if(left instanceof LocalDateTime && right instanceof LocalDateTime){
                result = ((LocalDateTime) left).compareTo((LocalDateTime) right) == 0;
            } else if (left instanceof LocalDate && right instanceof LocalDate){
                result = ((LocalDate) left).compareTo((LocalDate) right) == 0;
            } 
        }
        
        ValValue valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), result != null ? result.toString() : null);
        ValResult valResult = new ValResult(valueParentResult);
        valResult.setUsedMargin(false);
        return valResult;
    }
}
