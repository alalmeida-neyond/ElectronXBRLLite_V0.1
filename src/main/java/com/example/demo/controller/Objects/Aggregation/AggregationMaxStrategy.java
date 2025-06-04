package com.example.demo.controller.Objects.Aggregation;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;


public class AggregationMaxStrategy implements AggregationStrategy {

    @Override
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped) {
        boolean isToUseIntervals = false;
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
