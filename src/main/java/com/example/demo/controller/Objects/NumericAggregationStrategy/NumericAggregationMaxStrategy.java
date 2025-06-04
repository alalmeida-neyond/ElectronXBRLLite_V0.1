package com.example.demo.controller.Objects.NumericAggregationStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;

public class NumericAggregationMaxStrategy implements NumericAggregationStrategy {

    @Override
    public ValResult evaluate(ValNode parent, List<Map.Entry<ValNode, ValResult>> resultsGrouped) {
        boolean isToUseIntervals = false;
        BigDecimal valueMargin = BigDecimal.ZERO;
        BigDecimal maxResult = null; 
        BigDecimal marginResult = BigDecimal.ZERO; 
        ValValue valueParentResult = new ValValue();
        
        try {
            if(resultsGrouped != null && !resultsGrouped.isEmpty()){
                for(Map.Entry<ValNode, ValResult> pair : resultsGrouped){
                    valueMargin = BigDecimal.ZERO;
                    isToUseIntervals = OperationsUtils.isToUseMargin(pair.getKey(), pair.getValue());
                    if(isToUseIntervals){
                        valueMargin = OperationsUtils.setMarginValue(pair.getKey(), pair.getValue());
                    }
                    
                    ValResult resultTemp = (pair.getValue() == null || pair.getValue().getRawValue() == null)? OperationsUtils.applyDefaultValue(pair.getKey(), pair.getValue(), pair.getValue() != null ? pair.getValue().getDomain() : null) : pair.getValue();
                    BigDecimal value = (resultTemp == null || resultTemp.getRawValue() == null) ? null : new BigDecimal(resultTemp.getRawValue());

                    if ((value != null) && (maxResult == null || value.compareTo(maxResult) > 0)) {
                        maxResult = value;
                        marginResult = valueMargin;
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
