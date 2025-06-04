package com.example.demo.controller.Objects.Comparison;

import java.math.BigDecimal;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;


public class BooleanGreaterThanEqualToStrategy implements ComparisonStrategy {

    @Override
    public ValResult compare(Object left, Object right, boolean useIntervals, BigDecimal leftMargin, BigDecimal rightMargin) {     
        Boolean result = null;
        
        if (left != null && right != null) {
            result = ((Boolean) left).compareTo((Boolean) right) >= 0;   
        }
        
        ValValue valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), (result != null) ? result.toString() : null);
        ValResult valResult = new ValResult(valueParentResult);
        valResult.setUsedMargin(false);
        return valResult;
    }
}