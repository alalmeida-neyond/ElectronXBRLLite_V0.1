package com.example.demo.controller.Objects.Operations.Aggregation;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.DPMOrigin.DataType;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;

import java.util.logging.Level;
import java.util.logging.Logger;


public class AggregationMaxStrategy implements AggregationStrategy {

    private final static Logger LOG = Logger.getLogger(AggregationMaxStrategy.class.getName());

    @Override
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped) {
        boolean isToUseIntervals = false;
        boolean isFirstResult = true;
        DataType dataType = null;
        BigDecimal maxResult = null; 
        BigDecimal marginResult = BigDecimal.ZERO; 
        ValValue valueParentResult = new ValValue();

        try {
            if (operandChild != null) {
                //FIX ME to use stream.reduce
                for (ValResult result : resultsGrouped) {
                    if(!isFirstResult){
                        DataType valueDataType = (result != null ) ? ((result.getResult() != null ) ? result.getResult().getDatatype() : null) : null;
                        if(valueDataType == null || (dataType != null && valueDataType.getDataTypeId() != dataType.getDataTypeId())){
                            LOG.log(Level.SEVERE,"Operação de max realizada com diferentes tipos de dados, na regra " + operandChild.getOperationVersion().getOperationVID());
                            return null;
                        }
                    }
                    ValResult resultTemp = (result == null || result.getRawValue() == null)? OperationsUtils.applyDefaultValue(operandChild, result, result != null ? result.getDomain() : null) : result;
                    BigDecimal value = (resultTemp == null || resultTemp.getRawValue() == null) ? null : new BigDecimal(resultTemp.getRawValue());
                    BigDecimal valueMargin = BigDecimal.ZERO;
                    
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
                    
                    if(isFirstResult) {
                        isFirstResult = false;
                        dataType = (result != null ) ? ((result.getResult() != null ) ? result.getResult().getDatatype() : null) : null;
                    }
                }

                if(maxResult != null){
                    valueParentResult.setValue(maxResult.toPlainString());
                }
                
                valueParentResult.setDatatype(dataType);
                return new ValResult(valueParentResult, marginResult);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro ao realizar a operação de max no nó " + operandChild.getOperationVersion().getOperationVID(),e);
        }

        return null;
    }
}
