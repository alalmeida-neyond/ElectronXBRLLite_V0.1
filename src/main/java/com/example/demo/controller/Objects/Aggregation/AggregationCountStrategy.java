package com.example.demo.controller.Objects.Aggregation;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;

public class AggregationCountStrategy implements AggregationStrategy {

    @Override
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped) {
        BigDecimal countResult = BigDecimal.ZERO;
        ValValue valueParentResult = new ValValue();
        
        try {
            if(operandChild != null){
                for(ValResult result : resultsGrouped){
                    ValResult resultTemp = (result == null || result.getRawValue() == null)? OperationsUtils.applyDefaultValue(operandChild, result, result != null ? result.getDomain() : null) : result;
                    BigDecimal value = (resultTemp == null || resultTemp.getRawValue() == null) ? null : new BigDecimal(resultTemp.getRawValue());
                    if(value != null){
                        countResult = countResult.add(BigDecimal.ONE);
                    }
                }
                
                if(countResult != null){
                    valueParentResult.setValue(countResult.toPlainString());
                }
                
                valueParentResult.setDatatype(OperationsUtils.getDataTypeByID(Constants.DECIMAL));
                return new ValResult(valueParentResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
