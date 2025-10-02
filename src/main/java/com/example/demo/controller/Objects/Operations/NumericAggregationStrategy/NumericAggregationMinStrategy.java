package com.example.demo.controller.Objects.Operations.NumericAggregationStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.DPMOrigin.DataType;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NumericAggregationMinStrategy implements NumericAggregationStrategy {

    private final Logger LOG = Logger.getLogger(NumericAggregationMinStrategy.class.getName());

    @Override
    public ValResult evaluate(ValNode parent, List<Map.Entry<ValNode, ValResult>> resultsGrouped) {
        boolean isToUseIntervals = false;
        boolean isFirstResult = true;
        DataType dataType = null;
        BigDecimal valueMargin = BigDecimal.ZERO;
        BigDecimal minResult = null; 
        BigDecimal marginResult = BigDecimal.ZERO; 
        ValValue valueParentResult = new ValValue();
        
        try {
            if(resultsGrouped != null && !resultsGrouped.isEmpty()){
                for(Map.Entry<ValNode, ValResult> pair : resultsGrouped){
                    if(!isFirstResult){
                        DataType valueDataType = (pair.getValue() != null ) ? ((pair.getValue().getResult() != null ) ? pair.getValue().getResult().getDatatype() : null) : null;
                        if(valueDataType == null || (dataType != null && valueDataType.getDataTypeId() != Constants.DATATYPENOTAPPLICABLE && valueDataType.getDataTypeId() != dataType.getDataTypeId())){
                            LOG.log(Level.SEVERE,"Operação de min realizada com diferentes tipos de dados, na regra " + pair.getKey().getOperationVersion().getOperationVID());
                            return null;
                        }
                    }
                    valueMargin = BigDecimal.ZERO;
                    isToUseIntervals = OperationsUtils.isToUseMargin(pair.getKey(), pair.getValue());
                    if(isToUseIntervals){
                        valueMargin = OperationsUtils.setMarginValue(pair.getKey(), pair.getValue());
                    }
                    
                    ValResult resultTemp = (pair.getValue() == null || pair.getValue().getRawValue() == null)? OperationsUtils.applyDefaultValue(pair.getKey(), pair.getValue(), pair.getValue() != null ? pair.getValue().getDomain() : null) : pair.getValue();
                    BigDecimal value = (resultTemp == null || resultTemp.getRawValue() == null) ? null : new BigDecimal(resultTemp.getRawValue());

                    if ((value != null) && (minResult == null || value.compareTo(minResult) < 0)) {
                        minResult = value;
                        marginResult = valueMargin;
                    }
                    
                    if(isFirstResult || (dataType != null && dataType.getDataTypeId() == Constants.DATATYPENOTAPPLICABLE)) {
                        isFirstResult = false;
                        dataType = (pair.getValue() != null ) ? ((pair.getValue().getResult() != null ) ? pair.getValue().getResult().getDatatype() : null) : null;
                    }
                }
                
                if(minResult != null){
                    valueParentResult.setValue(minResult.toPlainString());
                }
                
                valueParentResult.setDatatype(dataType);
                return new ValResult(valueParentResult, marginResult);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro ao realizar a operação de AggregateMin " + parent.getOperationVersion().getOperationVID());          
        }

        return null;
    }
    
}
