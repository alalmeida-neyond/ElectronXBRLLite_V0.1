package com.example.demo.controller.Objects.Operations.Comparison;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;

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
