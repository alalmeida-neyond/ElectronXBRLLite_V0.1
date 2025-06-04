package com.example.demo.controller.Objects.Operations.Aggregation;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;


public class AggregationSumStrategy implements AggregationStrategy {

    @Override
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped) {
        boolean isToUseIntervals = false;
        BigDecimal valueMargin = BigDecimal.ZERO;
        BigDecimal sumResult = BigDecimal.ZERO;
        BigDecimal marginResult = BigDecimal.ZERO;
        ValValue valueParentResult = new ValValue();
        
        try {
            if(operandChild != null){
                for(ValResult result : resultsGrouped){
                    ValResult resultTemp = (result == null || result.getRawValue() == null)? OperationsUtils.applyDefaultValue(operandChild, result, result != null ? result.getDomain() : null) : result;
                    BigDecimal value = (resultTemp == null || resultTemp.getRawValue() == null) ? null : new BigDecimal(resultTemp.getRawValue());
                    valueMargin = BigDecimal.ZERO;
                    
                    isToUseIntervals = OperationsUtils.isToUseMargin(operandChild, resultTemp);
                    if(isToUseIntervals){
                        valueMargin = OperationsUtils.setMarginValue(operandChild, result);
                    }
                    
                    if(value != null){
                        sumResult = sumResult.add(value);
                        marginResult = marginResult.add(valueMargin);
                    }
                }
                
                if(sumResult != null){
                    valueParentResult.setValue(sumResult.toPlainString());
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
