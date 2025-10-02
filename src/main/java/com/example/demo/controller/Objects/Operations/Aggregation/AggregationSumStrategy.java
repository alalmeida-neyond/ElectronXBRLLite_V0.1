package com.example.demo.controller.Objects.Operations.Aggregation;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.DPMOrigin.DataType;
import com.example.demo.controller.Objects.Validation.OperationsUtils;
import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;
import com.example.demo.controller.Objects.Validation.ValValue;


public class AggregationSumStrategy implements AggregationStrategy {

    private final static Logger LOG = Logger.getLogger(AggregationSumStrategy.class.getName());

    @Override
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped) {
        boolean isToUseIntervals = false;
        boolean isFirstResult = true;
        DataType dataType = null;
        BigDecimal sumResult = BigDecimal.ZERO;
        BigDecimal marginResult = BigDecimal.ZERO;
        ValValue valueParentResult = new ValValue();
        
        try {
            if(operandChild != null){
                for(ValResult result : resultsGrouped){
                    if(!isFirstResult){
                        DataType valueDataType = (result != null ) ? ((result.getResult() != null ) ? result.getResult().getDatatype() : null) : null;
                        if(valueDataType == null || (dataType != null && valueDataType.getDataTypeId() != dataType.getDataTypeId())){
                            LOG.log(Level.SEVERE,"Operação de soma realizada com diferentes tipos de dados, na regra " + operandChild.getOperationVersion().getOperationVID());
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
                    
                    if(value != null){
                        sumResult = sumResult.add(value);
                        marginResult = marginResult.add(valueMargin);
                    }
                    
                    if(isFirstResult) {
                        isFirstResult = false;
                        dataType = (result != null ) ? ((result.getResult() != null ) ? result.getResult().getDatatype() : null) : null;
                    }
                }
                
                if(sumResult != null){
                    valueParentResult.setValue(sumResult.toPlainString());
                }
                
                valueParentResult.setDatatype(dataType);
                return new ValResult(valueParentResult, marginResult);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro ao realizar a operação de soma no nó " + operandChild.getOperationVersion().getOperationVID(),e);
        }
        
        return null;
    }
}
