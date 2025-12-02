package com.example.demo.controller.Objects.Validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.demo.Data.Access.Info;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.DPMOrigin.DataType;
import com.example.demo.controller.Objects.Entities.DPMOrigin.Operator;
import com.example.demo.controller.Objects.Operations.Aggregation.*;
import com.example.demo.controller.Objects.Operations.Comparison.*;
import com.example.demo.controller.Objects.Operations.IndividualBoolean.*;
import com.example.demo.controller.Objects.Operations.Logical.*;
import com.example.demo.controller.Objects.Operations.NumericAggregationStrategy.*;
import com.example.demo.controller.Objects.Operations.Where.*;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ValidationOperators {
    
    private final static Logger LOG = Logger.getLogger(ValidationOperators.class.getName());
    private final static OperationExpressionBuilder expressionBuilder = new OperationExpressionBuilder();
    private final static OperationDomainBuilder domainBuilder = new OperationDomainBuilder();

    public static Boolean evaluate(ValNode parent, List<ValNode> childs, String entityID, String domain) {
        Operator operator = parent.getNode().getOperator();
        switch (operator.getOperatorID()) {
            //subgrupo dos númericos
            case Constants.ADDITION: //2
            case Constants.SUBSTRACTION: //5
            case Constants.MULTIPLICATION: //8
            case Constants.DIVISION: //3
                return evaluateBinaryOperations(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTLEFT), OperationsUtils.getChildNode(childs, Constants.ARGUMENTRIGHT), Constants.ARITHMETICSUBGROUP);

            //subgrupo dos numericos individuais
            case Constants.UNARYPLUS: //1
            case Constants.ABSOLUTEVALUE: //6
            case Constants.UNARYMINUS: //4
            case Constants.SQUAREROOT: //10
                return evaluateIndividualOperations(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTOPERAND), Constants.INDIVIDUALNUMERICSUBGROUP);

            //operadores de comparacao
            case Constants.EQUALSTO: //13
            case Constants.LESSTHANEQUALTO: //14
            case Constants.GREATERTHANEQUALTO: //15
            case Constants.GREATERTHAN: //18
            case Constants.LESSTHAN: //19
            case Constants.NOTEQUALTO: //20
                return evaluateBinaryOperations(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTLEFT), OperationsUtils.getChildNode(childs, Constants.ARGUMENTRIGHT), Constants.COMPARISONSUBGROUP);

            //operadores de logica
            case Constants.AND: //22
            case Constants.OR: //23
            case Constants.EXCLUSIVEOR: //25
                return evaluateBinaryOperations(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTLEFT), OperationsUtils.getChildNode(childs, Constants.ARGUMENTRIGHT), Constants.LOGICALSUBGROUP);

            //operadores booleanos individuais
            case Constants.NOT: //17
            case Constants.ISNULL: //24
                return evaluateIndividualOperations(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTOPERAND), Constants.INDIVIDUALBOOLEANSUBGROUP);

            //operadores de agregacao
            case Constants.AGGREGATEMAXIMUM: //11
            case Constants.AGGREGATEMINIMUM: //12
            case Constants.SUM: //26
            case Constants.COUNT: //27
                return evaluateAggregateOperations(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTOPERAND), OperationsUtils.getChildNode(childs, Constants.ARGUMENTGROUPINGCLAUSE));
                
            case Constants.NUMERICMINIMUM: //7
            case Constants.NUMERICMAXIMUM: //9
                return evaluateAggregateNumericOperations(parent, childs);
                
                
            //operadores com comportamentos proprios  
            case Constants.ELEMENTOF: //16
                return elementOf(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTOPERAND),  OperationsUtils.getChildNode(childs, Constants.ARGUMENTSET));
            case Constants.MATCHCARACTERS: //21
                return matchCaracters(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTOPERAND), OperationsUtils.getChildNode(childs, Constants.ARGUMENTPATTERN));
            case Constants.WHERE: //28
                return where(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTOPERAND), OperationsUtils.getChildNode(childs, Constants.ARGUMENTCONDITION));
            case Constants.GET: //29
                return get(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTOPERAND), OperationsUtils.getChildNode(childs, Constants.ARGUMENTCOMPONENT));
            case Constants.IFTHENELSE: //30
                return ifThenElse(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTCONDITION), OperationsUtils.getChildNode(childs, Constants.ARGUMENTTHEN), OperationsUtils.getChildNode(childs, Constants.ARGUMENTELSE));
            case Constants.FILTER: //31
                return filter(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTSELECTION), OperationsUtils.getChildNode(childs, Constants.ARGUMENTCONDITION));
            case Constants.TIMESHIFT: //32
                return timeShift(parent, OperationsUtils.getChildNode(childs, Constants.ARGUMENTOPERAND), 
                                            OperationsUtils.getChildNode(childs, Constants.ARGUMENTPERIOD),
                                            OperationsUtils.getChildNode(childs, Constants.ARGUMENTNUMBER),
                                            OperationsUtils.getChildNode(childs, Constants.ARGUMENTDIMENSION), 
                                            entityID,
                                            domain);
            case Constants.GROUPINGCLAUSE: //35
                return groupingClause(parent, childs);
            case Constants.PARENTHESISEXPRESSION: //37
                return parenthesis(parent, childs.get(Constants.FIRSTRESULT));
            default:
                return false;
        }
    }

    //Preparacao para as operacoes binarias
    private static boolean evaluateBinaryOperations(ValNode parent, ValNode leftChild, ValNode rightChild, int operationsSubgroup) {
        List<ValResult> leftValues = new ArrayList<>();
        List<ValResult> rightValues = new ArrayList<>();
        boolean leftIsScalar = false;
        boolean rightIsScalar = false;
        boolean isToCompareToItem = false;

        try {
            
            //Caso: Tem scalar
            leftIsScalar = leftChild.getNode().getScalar() != null;
            rightIsScalar = rightChild.getNode().getScalar() != null;
            isToCompareToItem = OperationsUtils.isToCompareWithItem(rightChild);
            
            if (OperationsUtils.isChildOfWhere(parent)) {
                return OperationsUtils.applyChildWhereLogic(parent, leftChild, rightChild);
            }
            
            if (leftIsScalar && rightIsScalar) {
                ValValue leftScalarValue = new ValValue(leftChild.getNode().getScalar());
                ValResult leftScalarResult = new ValResult(leftScalarValue);
                leftValues.add(leftScalarResult);
                leftChild.setResults(leftValues);

                ValValue rightScalarValue = new ValValue(rightChild.getNode().getScalar());
                ValResult rightScalarResult = new ValResult(rightScalarValue);
                rightValues.add(rightScalarResult);
                rightChild.setResults(rightValues);
                

                return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
            } else if (leftIsScalar) {
                ValValue scalarValue = new ValValue(leftChild.getNode().getScalar());
                ValResult scalarResult = new ValResult(scalarValue);

                for (ValResult rightValue : rightChild.getResults()) {
                    leftValues.add(scalarResult);
                }
                leftChild.setResults(leftValues);
                
                return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
            } else if (rightIsScalar) {
                ValValue scalarValue = new ValValue(rightChild.getNode().getScalar());
                ValResult scalarResult = new ValResult(scalarValue);

                for (ValResult leftValue : leftChild.getResults()) {
                    rightValues.add(scalarResult);
                }
                rightChild.setResults(rightValues);
                
                return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
            } else if (isToCompareToItem) {
                ValResult itemResult = rightChild.getResults().get(Constants.FIRSTRESULT);

                for (ValResult leftValue : leftChild.getResults()) {
                    rightValues.add(itemResult);
                }
                rightChild.setResults(rightValues);
                
                return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
            }

            Boolean isToUseKeys = OperationsUtils.isToUseKeys(leftChild, rightChild);
            if (isToUseKeys == null){
                boolean leftHasKeys = OperationsUtils.hasKeys(leftChild);
                boolean rightHasKeys = OperationsUtils.hasKeys(rightChild);
                
                leftValues = leftChild.getResults();
                rightValues = rightChild.getResults();
                
                if(leftHasKeys && !rightHasKeys && leftValues.size() >= 1 && rightValues.size() == 1){
                    ValResult uniqueResult = rightValues.get(Constants.FIRSTRESULT);
                    rightValues.clear();
                    
                    for (ValResult leftValue : leftChild.getResults()) {
                        rightValues.add(uniqueResult);
                    }
                    rightChild.setResults(rightValues);
                    return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
                } else if(rightHasKeys && !leftHasKeys && rightValues.size() >= 1 && leftValues.size() == 1){
                    ValResult uniqueResult = leftValues.get(Constants.FIRSTRESULT);
                    leftValues.clear();
                    
                    for (ValResult rightValue : rightChild.getResults()) {
                        leftValues.add(uniqueResult);
                    }
                    leftChild.setResults(leftValues);
                    return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
                } else {
                    //DEU UM ERRO
                    return false;
                }
            } else if (isToUseKeys) {
                return prepareBinaryOperation(parent, leftChild, rightChild, true, operationsSubgroup);
            }

            //Caso: Normal, sem ser com scalar nem chaves
            return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }

    //Preparacao para as operacoes de operandos individuais
    private static boolean evaluateIndividualOperations(ValNode parent, ValNode operandChild, int operationsSubgroup) {
        List<ValResult> operandValues = new ArrayList<>();
        boolean isScalar = false;

        try {
            isScalar = operandChild.getNode().getScalar() != null;

            if (isScalar) {
                ValValue scalarValue = new ValValue(operandChild.getNode().getScalar());
                ValResult scalarResult = new ValResult(scalarValue);
                operandValues.add(scalarResult);
                operandChild.setResults(operandValues);
            }

            return prepareIndividualOperations(parent, operandChild, operationsSubgroup);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }

    //Avaliacao das operacoes de agregacao
    private static boolean evaluateAggregateOperations(ValNode parent, ValNode operandChild, ValNode groupingClauseChild) {
        List<ValResult> operandValues = new ArrayList<>();
        List<ValResult> parentResults = new ArrayList<>();
        ValResult parentResult = new ValResult();

        try {
            if(operandChild != null && operandChild.getResults() != null && !operandChild.getResults().isEmpty()){
                operandValues = operandChild.getResults();
                
                if (groupingClauseChild != null && groupingClauseChild.getResults() != null) {
                    
                    //ir buscar a chave que agrupa os valores
                    ValResult resultFromGroup = groupingClauseChild.getResults().get(Constants.FIRSTRESULT);
                    ValKey key = resultFromGroup.getKey();

                    //agrupa os valores
                    Map<ValKey, List<ValResult>> valuesGroupedBy = OperationsUtils.groupValues(operandValues, key, operandChild);
                    if(valuesGroupedBy == null || valuesGroupedBy.isEmpty()){
                        LOG.log(Level.SEVERE,"Ocorreu um erro no agrupamento dos valores, no No " + parent.getNode().getNodeID());
                        return false;
                    }

                    //percorre os valores agrupados
                    for(ValKey keyThatGroupValues : valuesGroupedBy.keySet()){
                        List<ValResult> resultsThatShareKey = valuesGroupedBy.get(keyThatGroupValues);
                        
                        
                        AggregationStrategy strategy = AggregationStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
                        parentResult = strategy.evaluate(operandChild, resultsThatShareKey);
                        parentResult.setKey(keyThatGroupValues);
                                                
                        //verifica se o resultado e nulo, pois os nulos devem ser ignorados
                        if (!parentResult.valueIsNull()) {
                            parentResult.setExpression(expressionBuilder.aggregateOperationBuilder(parent.getOperatorSymbol(), operandChild, groupingClauseChild, resultsThatShareKey, resultFromGroup));
                            parentResult.setDomain(domainBuilder.groupOfResultsDomainBuilder(resultsThatShareKey));
                            parentResults.add(parentResult);
                        } 
                    }
                } else {
                                        
                    AggregationStrategy strategy = AggregationStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
                    parentResult = strategy.evaluate(operandChild, operandValues);

                    //verifica se o resultado e nulo, pois os nulos devem ser ignorados
                    if (!parentResult.valueIsNull()) {
                        parentResult.setExpression(expressionBuilder.aggregateOperationBuilder(parent.getOperatorSymbol(), operandChild, null, operandValues, null));
                        parentResult.setDomain(domainBuilder.groupOfResultsDomainBuilder(operandValues));
                        parentResults.add(parentResult);
                    } 
                }
                
                //Se o resultado dos pais for diferente de vazio, entao adiciona os resultados no pai
                if(!parentResults.isEmpty()){
                    parent.setResults(parentResults);
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }
    
    private static Boolean evaluateAggregateNumericOperations(ValNode parent, List<ValNode> childs) {
        List<ValResult> values = new ArrayList<>();
        List<ValResult> parentResults = new ArrayList<>();
        
        try {
            if(childs != null && !childs.isEmpty()){
                childs.forEach(child -> {
                    if(child.getResults() == null && child.getNode().getScalar() != null){
                        DataType dataType = new DataType();
                        dataType.setDataTypeId(Constants.DATATYPENOTAPPLICABLE);
                        values.add(new ValResult(new ValValue(dataType, child.getNode().getScalar())));
                    }
                    if(child.getResults() != null && !child.getResults().isEmpty()){
                        values.addAll(child.getResults());
                    }
                });
                
                if(!values.isEmpty()){
                    if(OperationsUtils.isToUseKeys(childs)){                        
                        //agrupamento dos valores
                        ValKey groupingKey = OperationsUtils.determineGroupingKey(values);
                        Map<ValKey, List<Map.Entry<ValNode, ValResult>>> results = OperationsUtils.groupValuesOfNodes(childs, groupingKey);
                        
                        for(ValKey keyToUse : results.keySet()){
                            List<Map.Entry<ValNode, ValResult>> valuesGrouped = results.get(keyToUse);
                            
                            ValResult parentResult = applyNumericAggregateOperation(parent, valuesGrouped, keyToUse);
                            parentResult.setExpression(expressionBuilder.aggregateNumericOperationBuilder(parent.getOperatorSymbol(), valuesGrouped));
                            parentResult.setDomain(domainBuilder.aggregateNumericDomainBuilder(valuesGrouped));
                            parentResults.add(parentResult);
                        }
                    } else {
                        Map<ValKey, List<Map.Entry<ValNode, ValResult>>> results = OperationsUtils.groupValuesOfNodes(childs, null);
                                              
                        ValResult parentResult = applyNumericAggregateOperation(parent, results.get(null) , null);
                        parentResult.setExpression(expressionBuilder.aggregateNumericOperationBuilder(parent.getOperatorSymbol(), results.get(null)));
                        parentResult.setDomain(domainBuilder.aggregateNumericDomainBuilder(results.get(null)));
                        parentResults.add(parentResult);
                    }
                    
                    parent.setResults(parentResults);
                    return true;
                } else {
                    LOG.log(Level.SEVERE,"Nao foram encontrados valores no filho do No " + parent.getNode().getNodeID());
                    return false;
                }
            } else {
                LOG.log(Level.SEVERE,"Nao foram encontrados filhos do No " + parent.getNode().getNodeID());
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }
        return false;
    }

    //Percorre os valores e consoante o subgrupo, aplica a operacao
    private static boolean prepareIndividualOperations(ValNode parent, ValNode operandChild, int operationsSubgroup) {
        List<ValResult> resultsToParent = new ArrayList<>();
        List<ValResult> operandValues = new ArrayList<>();
        ValResult operandValue = new ValResult();
        ValResult parentResult = new ValResult();
        ValKey keyToUse = new ValKey();
        boolean isToUseIntervals = false;
        BigDecimal margin = BigDecimal.ZERO;
        boolean isScalar = false;

        try {
            operandValues = operandChild.getResults();
            isScalar = operandChild.getNode().getScalar() != null;

            //Vai percorrer a lista
            for (int i = 0; i < operandValues.size(); i++) {
                operandValue = operandValues.get(i);

                //Caso seja nulo, deve usar o defaultvalue
                if (operandValue == null || operandValue.valueIsNull()) {
                    operandValue = OperationsUtils.applyDefaultValue(operandChild, operandValue, operandValue != null ? operandValue.getDomain() : null);
                }

                keyToUse = operandValue.getKey();
                isToUseIntervals = OperationsUtils.isToUseMargin(operandChild, operandValue);
                if (isToUseIntervals) {
                    
                    //Caso estejam a zero (valor idêntico a null vindo da base de dados), deve ser para utilizar margem de erro parametrizada
                    margin = OperationsUtils.setMarginValue(operandChild, operandValue);
                }

                switch (operationsSubgroup) {
                    case Constants.INDIVIDUALNUMERICSUBGROUP:
                        parentResult = applyIndividualNumericOperation(parent, operandValue, keyToUse, isToUseIntervals, margin, isScalar);
                        break;
                    case Constants.INDIVIDUALBOOLEANSUBGROUP:
                        parentResult = applyIndividualBooleanOperation(parent, operandValue, keyToUse);
                        break;
                }

                if (parentResult != null) {
                    parentResult.setExpression(expressionBuilder.individualOperationBuilder(parent.getOperatorSymbol(), operandChild, operandValue));
                    parentResult.setDomain(domainBuilder.individualResultDomainBuilder(operandValue));
                    resultsToParent.add(parentResult);
                } else {
                    LOG.log(Level.SEVERE,"Ocorreu um erro num dos applyOperations, no No " + parent.getNode().getNodeID());
                    return false;
                }
            }

            if (isScalar) {
                parent.getNode().setScalar(resultsToParent.get(0).getRawValue());
            } else {
                parent.setResults(resultsToParent);
            }
            return true;

        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }

    //Consoante a utilizacao de chaves ou nao, percorre os valores e envia-os para eles serem avaliados com operacoes numericas
    private static boolean prepareBinaryOperation(ValNode parent, ValNode leftNode, ValNode rightNode, boolean isToUseKeys, int operationsSubgroup) {
        List<ValResult> resultsToParent = new ArrayList<>();
        List<ValResult> leftValues = new ArrayList<>();
        List<ValResult> rightValues = new ArrayList<>();
        Map<ValResult, List<ValResult>> groupedValues = new HashMap<>();
        ValResult leftValue = new ValResult();
        ValResult rightValue = new ValResult();
        ValResult parentResult = new ValResult();
        boolean leftIsScalar = false;
        boolean rightIsScalar = false;

        try {
            leftIsScalar = leftNode.getNode().getScalar() != null;
            rightIsScalar = rightNode.getNode().getScalar() != null;

            leftValues = leftNode.getResults();
            rightValues = rightNode.getResults();

            //Caso seja para usar chaves (aplicar abordagem do "inner join"
            if (isToUseKeys) {
                
                ValKey innerJoinKey = OperationsUtils.defineInnerJoinKeys(leftValues, rightValues);

                if (innerJoinKey == null || innerJoinKey.hasKeysPropertiesIndexsNull()) {
                    LOG.log(Level.SEVERE,"Nao foi encontrada uma 'inner key', no No " + parent.getNode().getNodeID());
                    return false;
                }

                //Agrupa os dados, simulando o "innerJoin"
                groupedValues = OperationsUtils.groupValuesByInnerJoin(leftValues, rightValues, leftNode,rightNode, innerJoinKey);

                if (groupedValues.isEmpty()) {
                    LOG.log(Level.SEVERE,"Nao conseguiu agrupar os valores, no No " + parent.getNode().getNodeID());
                    return false;
                }

                //Comeca a percorrer os valores agrupados
                for (Map.Entry<ValResult, List<ValResult>> entry : groupedValues.entrySet()) {
                    //Se o da esquerda for null, usar o valor default
                    leftValue = entry.getKey();
                    if (leftValue == null || leftValue.valueIsNull()) {
                        leftValue = OperationsUtils.applyDefaultValue(leftNode, leftValue, leftValue != null ? leftValue.getDomain() : null);
                    }

                    //Valores que partilham a mesma chave que o da esquerda
                    for (ValResult matchedValue : entry.getValue()) {

                        //Se o da direita for null, usar o valor default
                        rightValue = matchedValue;
                        if (rightValue == null || rightValue.valueIsNull()) {
                            rightValue = OperationsUtils.applyDefaultValue(rightNode, rightValue, rightValue != null ? rightValue.getDomain() : null);
                        }

                        //Aplica a operacao aos valores
                        switch (operationsSubgroup) {
                            case Constants.ARITHMETICSUBGROUP:
                                parentResult = applyNumericOperations(parent, leftNode, rightNode, leftValue, rightValue);
                                break;
                            case Constants.COMPARISONSUBGROUP:
                                parentResult = applyComparisonOperations(parent, leftNode, rightNode, leftValue, rightValue);
                                break;
                            case Constants.LOGICALSUBGROUP:
                                parentResult = applyLogicalOperations(parent, leftNode, rightNode, leftValue, rightValue);
                                break;
                            default:
                                //Subgrupo desconhecido.
                                throw new AssertionError();
                        }

                        if (parentResult != null) {
                            parentResult.setExpression(expressionBuilder.binaryOperationBuilder(parent.getOperatorSymbol(), leftNode, rightNode, leftValue, rightValue));
                            parentResult.setDomain(domainBuilder.binaryOperationDomainBuilder(leftValue, rightValue));
                            resultsToParent.add(parentResult);
                        } else {
                            LOG.log(Level.SEVERE,"Ocorreu um erro num dos applyOperations, no No " + parent.getNode().getNodeID());
                        }

                    }
                }
            } else {
                //Caso em que nao se usa chaves (Ou sao comparados dois valores apenas, ou sao comparados valores com scalars)
                for (int i = 0; i < leftValues.size(); i++) {
                    //Caso o da eaquerda seja null, usar o default
                    leftValue = leftValues.get(i);
                    if (leftValue == null || leftValue.valueIsNull()) {
                        leftValue = OperationsUtils.applyDefaultValue(leftNode, leftValue, leftValue != null ? leftValue.getDomain() : null);
                    }

                    //Caso o da direita seja null, usar o default
                    rightValue = rightValues.isEmpty() ? null : rightValues.get(i);
                    if (rightValue == null || rightValue.valueIsNull()) {
                        rightValue = OperationsUtils.applyDefaultValue(rightNode, rightValue, rightValue != null ? rightValue.getDomain() : null);
                    }

                    //Aplica a operacao aos valores
                    switch (operationsSubgroup) {
                        case Constants.ARITHMETICSUBGROUP:
                            parentResult = applyNumericOperations(parent, leftNode, rightNode, leftValue, rightValue);
                            break;
                        case Constants.COMPARISONSUBGROUP:
                            parentResult = applyComparisonOperations(parent, leftNode, rightNode, leftValue, rightValue);
                            break;
                        case Constants.LOGICALSUBGROUP:
                            parentResult = applyLogicalOperations(parent, leftNode, rightNode, leftValue, rightValue);
                            break;
                        default:
                            //Subgrupo desconhecido.
                            throw new AssertionError();
                    }

                    if (parentResult != null) {
                        parentResult.setExpression(expressionBuilder.binaryOperationBuilder(parent.getOperatorSymbol(), leftNode, rightNode, leftValue, rightValue));
                        parentResult.setDomain(domainBuilder.binaryOperationDomainBuilder(leftValue, rightValue));
                        resultsToParent.add(parentResult);
                    } else {
                        LOG.log(Level.SEVERE,"Ocorreu um erro num dos applyOperations, no No " + parent.getNode().getNodeID());
                        return false;
                    }
                }
            }

            //Caso sejam dois scalars, ele deve enviar para cima o scalar
            if (leftIsScalar && rightIsScalar) {
                parent.getNode().setScalar(resultsToParent.get(0).getRawValue());
            } else {
                parent.setResults(resultsToParent);
            }
            return true;

        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }

    //Aplicacao das operacoes numericas sobre os valores enviados como parâmetros
    private static ValResult applyNumericOperations(ValNode parent, ValNode leftNode, ValNode rightNode, ValResult leftValue, ValResult rightValue) {
        BigDecimal leftMargin = null;
        BigDecimal rightMargin = null;
        boolean isToUseIntervals = false;
        ValValue valueParentResult = new ValValue();
        ValResult parentResult = new ValResult();
        BigDecimal finalMargin = BigDecimal.ZERO;
        Integer precisionOfDivision = 0;
        ValKey keyToUse = new ValKey();
        String marginsLog = null;
        
        try {
            boolean leftIsScalar = leftNode.getNode().getScalar() != null;
            boolean rightIsScalar = rightNode.getNode().getScalar() != null;

            precisionOfDivision = Integer.valueOf(Info.getInstance().getConfigValueByKey(Constants.PRECISIONOFDIVISION));

            //Caso sejam diferentes de nulo, constroi o big decimal com o valor, senao mantem a nulo
            BigDecimal left = (leftValue == null) ? null : (leftValue.getRawValue() == null) ? null : new BigDecimal(leftValue.getRawValue());
            BigDecimal right = (rightValue.getRawValue() == null) ? null : new BigDecimal(rightValue.getRawValue());

            //Caso algum seja nulo, o resultado deve ser nulo
            if (left == null || right == null) {
                
                return OperationsUtils.createNullResult(leftValue, rightValue, leftIsScalar, rightIsScalar);                
            } else {
                //Caso seja para utilizar margem de erro
                isToUseIntervals = OperationsUtils.isToUseMargin(leftNode, leftValue) || OperationsUtils.isToUseMargin(rightNode, rightValue);
                if (isToUseIntervals) {
                    //Caso estejam a zero (valor idêntico a null vindo da base de dados), deve ser para utilizar margem de erro parametrizada
                    leftMargin = OperationsUtils.setMarginValue(leftNode, leftValue);
                    rightMargin = OperationsUtils.setMarginValue(rightNode, rightValue);
                    
                    marginsLog = "Esquerda -> " + leftMargin.toPlainString() + ", Direita -> " +rightMargin.toPlainString();
                }
            }
            
            DataType resultDataType = OperationsUtils.determineDataType(leftValue, rightValue, leftIsScalar, rightIsScalar);
            if(resultDataType != null){
            //resultLog = left+ " "+ parent.getNode().getOperator().getSymbol() + " "+right;
                switch (parent.getNode().getOperator().getOperatorID()) {
                    case Constants.ADDITION:
                        if (isToUseIntervals) {
                            //Coloca o valor da margem no pai
                            if(leftMargin != null && rightMargin != null){
                                finalMargin = leftMargin.add(rightMargin);
                            } 
                        }

                        //Faz a operação e coloca o resultado na lista de reusltados do pai
                        valueParentResult = new ValValue(resultDataType, left.add(right).toPlainString());
                        break;
                    case Constants.SUBSTRACTION:
                        if (isToUseIntervals) {
                            //Coloca o valor da margem no pai
                            if(leftMargin != null && rightMargin != null){
                                finalMargin = leftMargin.add(rightMargin);
                            } 
                        }

                        //Faz a operação e coloca o resultado na lista de reusltados do pai
                        valueParentResult = new ValValue(resultDataType, left.subtract(right).toPlainString());
                        break;
                    case Constants.DIVISION:
                        if (isToUseIntervals) {
                            List<BigDecimal> margins = Arrays.asList(
                                    left.add(leftMargin).divide(right.add(rightMargin), precisionOfDivision, RoundingMode.HALF_UP),
                                    left.add(leftMargin).divide(right.subtract(rightMargin), precisionOfDivision, RoundingMode.HALF_UP),
                                    left.subtract(leftMargin).divide(right.add(rightMargin), precisionOfDivision, RoundingMode.HALF_UP),
                                    left.subtract(leftMargin).divide(right.subtract(rightMargin), precisionOfDivision, RoundingMode.HALF_UP)
                            );

                            finalMargin = Collections.max(margins);
                        }


                        try {
                            //Faz a operação
                            if (right == BigDecimal.ZERO) {
                                LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID());
                            } else {
                                valueParentResult = new ValValue(resultDataType, left.divide(right, precisionOfDivision, RoundingMode.HALF_UP).toPlainString());
                            }
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
                            return null;
                        }

                        break;
                    case Constants.MULTIPLICATION:
                        if (isToUseIntervals) {
                            //Coloca o valor da margem no pai
                            if(leftMargin != null && rightMargin != null){
                                List<BigDecimal> margins = Arrays.asList(
                                    left.multiply(rightMargin),
                                    leftMargin.multiply(right).abs(),
                                    leftMargin.multiply(rightMargin)
                                );

                                finalMargin = BigDecimal.ZERO;
                                for (BigDecimal number : margins) {
                                    finalMargin = finalMargin.add(number);
                                }
                            }
                        }

                        //Faz a operação e coloca o resultado na lista de reusltados do pai
                        valueParentResult = new ValValue(resultDataType, left.multiply(right).toPlainString());
                        break;
                    default:
                        //Operador desconhecido
                        throw new AssertionError();
                }
            

                //constrói a chave
                keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);

                //adiciona o resultado ao pai
                parentResult = new ValResult(keyToUse, valueParentResult, finalMargin);
                return parentResult;
            } else {
                LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " , relacionado com os tipos de dados.");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return null;
    }

    //Aplicacao das operacoes de comparacao sobre os valores enviados como parâmetros
    private static ValResult applyComparisonOperations(ValNode parent, ValNode leftNode, ValNode rightNode, ValResult leftValue, ValResult rightValue) {
        boolean isToUseIntervals = false;
        BigDecimal leftMargin = null;
        BigDecimal rightMargin = null;
        DataType leftDataType = null;
        DataType rightDataType = null;
        DataType resultDataType = null;
        Object left = null;
        Object right = null;
        ValValue valueParentResult = new ValValue();
        ValResult parentResult = new ValResult();
        ValKey keyToUse = new ValKey();

        boolean leftIsScalar = false;
        boolean rightIsScalar = false;
        
        String marginLog = "";

        try {
            
            leftIsScalar = leftNode.getNode().getScalar() != null;
            rightIsScalar = rightNode.getNode().getScalar() != null;

            if (leftValue.getResult() == null || leftValue.valueIsNull() || rightValue.getResult() == null || rightValue.valueIsNull()) {
                
                return OperationsUtils.createNullResult(leftValue, rightValue, leftIsScalar, rightIsScalar);
            }

            leftDataType = OperationsUtils.determineDataType(leftValue, rightValue, leftIsScalar, rightIsScalar);
            if(leftDataType == null){
                return null;
            }
            left = OperationsUtils.transformValue(leftValue, leftDataType, leftNode);

            rightDataType = OperationsUtils.determineDataType(leftValue, rightValue, leftIsScalar, rightIsScalar);
            if(rightDataType == null){
                return null;
            }
            right = OperationsUtils.transformValue(rightValue, rightDataType, rightNode);
            
            resultDataType = OperationsUtils.determineDataType(leftDataType, rightDataType);
            if(resultDataType == null){
                return null;
            }
            isToUseIntervals = OperationsUtils.isToUseMargin(leftNode, leftValue) || OperationsUtils.isToUseMargin(rightNode, rightValue);

            if (isToUseIntervals) {
                
                leftMargin = OperationsUtils.setMarginValue(leftNode, leftValue);
                rightMargin = OperationsUtils.setMarginValue(rightNode, rightValue);
                
                marginLog = "Esquerda -> " + leftMargin + ", Direita -> " +rightMargin;
            }

            
            ComparisonStrategy strategy = ComparisonStrategyFactory.getStrategy(resultDataType.getDataTypeId(), parent.getNode().getOperator().getOperatorID());
            parentResult = strategy.compare(left, right, isToUseIntervals, leftMargin, rightMargin);

            //constroi a chave
            keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);
            
            //adiciona o resultado ao pai
            parentResult.setKey(keyToUse);
            return parentResult;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return null;
    }

    //Aplicacao das operacoes de logicas sobre os valores enviados como parâmetros
    private static ValResult applyLogicalOperations(ValNode parent, ValNode leftNode, ValNode rightNode, ValResult leftValue, ValResult rightValue) {
        Boolean left = null;
        Boolean right = null;
        ValValue valueParentResult = new ValValue();
        ValResult parentResult = new ValResult();
        ValKey keyToUse = new ValKey();
        
        String resultLog = "";

        boolean rightIsScalar = false;
        boolean leftIsScalar = false;

        try {
            rightIsScalar = rightNode.getNode().getScalar() != null;
            leftIsScalar = leftNode.getNode().getScalar() != null;

            if (leftValue.getResult() == null || leftValue.valueIsNull() || rightValue.getResult() == null || rightValue.valueIsNull()) {
                return OperationsUtils.createNullResult(leftValue, rightValue, leftIsScalar, rightIsScalar);
            }

            left = Boolean.valueOf(leftValue.getRawValue());
            right = Boolean.valueOf(rightValue.getRawValue());
            
            LogicalStrategy strategy = LogicalStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
            Boolean result = strategy.compare(left, right);

            valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEBOOLEAN), (result != null) ? result.toString() : null);

            //constroi a chave
            keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);
            
            //adiciona o resultado ao pai
            parentResult = new ValResult(keyToUse, valueParentResult);
            return parentResult;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return null;
    }

    //Aplicacao das operacoes sobre operandos individuais que retornam booleanos
    private static ValResult applyIndividualBooleanOperation(ValNode parent, ValResult operandValue, ValKey keyToUse) {
        String operand = null;
        ValValue valueParentResult = new ValValue();
        ValResult parentResult = new ValResult();
        Boolean result = null;
        
        try {
            
            operand = operandValue.getRawValue();
            
            IndividualBooleanStrategy strategy = IndividualBooleanStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
            result = strategy.evaluate(operand);

            valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEBOOLEAN), (result != null) ? result.toString() : null);
            parentResult = new ValResult(keyToUse, valueParentResult);
            return parentResult;
        
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return null;
    }

    //Aplicacao das operacoes sobre operandos individuais que retornam numericos
    private static ValResult applyIndividualNumericOperation(ValNode parent, ValResult operandValue, ValKey keyToUse, boolean isToUseIntervals, BigDecimal margin, Boolean isScalar) {
        String resultLog = "";
        ValValue valueParentResult = new ValValue();
        BigDecimal finalMargin = BigDecimal.ZERO;
        DataType resultDataType = null;

        try {
            
            //Caso sejam diferentes de nulo, constroi o big decimal com o valor, senao mantem a nulo
            BigDecimal operand = (operandValue.getRawValue() == null) ? null : new BigDecimal(operandValue.getRawValue());

            //Caso algum seja nulo, o resultado deve ser nulo
            resultLog = parent.getNode().getOperator().getSymbol() + " " +operand;
            if (operand == null) {
                return new ValResult(keyToUse, new ValValue((operandValue.getResult() != null) ? operandValue.getResult().getDatatype() : null, null), operandValue.getRefDate(), operandValue.getMargin(), operandValue.getDomain());
            }
            
            if(isScalar){
                DataType dataType = new DataType();
                dataType.setDataTypeId(Constants.DATATYPENOTAPPLICABLE);
                resultDataType = dataType;
            } else {
                resultDataType = operandValue.getResult().getDatatype();
            }

            if(resultDataType != null){
                switch (parent.getNode().getOperator().getOperatorID()) {
                    //Faz a operação e coloca o resultado na lista de reusltados do pai
                    case Constants.UNARYPLUS:
                        valueParentResult = new ValValue(resultDataType, operand.toPlainString());
                        if (isToUseIntervals) {
                            finalMargin = margin;
                        }
                        break;
                    case Constants.ABSOLUTEVALUE:
                        valueParentResult = new ValValue(resultDataType, operand.abs().toPlainString());
                        if (isToUseIntervals) {
                            finalMargin = margin;
                        }
                        break;
                    case Constants.UNARYMINUS:
                        valueParentResult = new ValValue(resultDataType, operand.negate().toPlainString());
                        if (isToUseIntervals) {
                            finalMargin = margin;
                        }
                        break;
                    case Constants.SQUAREROOT:
                        double result = Math.sqrt(operand.doubleValue());
                        if(result <= 0.0){
                            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID());
                            return null;
                        }
                        valueParentResult = new ValValue(resultDataType, result + "");

                        break;
                    default:
                        //Operador desconhecido.
                        throw new AssertionError();
                }

                //adiciona o resultado ao pai
                return new ValResult(keyToUse, valueParentResult,operandValue.getRefDate(), finalMargin, operandValue.getDomain());
            } else {
                LOG.log(Level.SEVERE, "Ocorreu um erro no nó " + parent.getNode().getNodeID() + " , relacionado com os tipos de dados.");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return null;
    }
    
    private static ValResult applyNumericAggregateOperation(ValNode parent, List<Map.Entry<ValNode, ValResult>> results, ValKey keyToUse) {
        boolean isToUseKeys = false;
        ValResult parentResult = new ValResult();

        try {
            if (results != null && !results.isEmpty()) {
                isToUseKeys = keyToUse != null && !keyToUse.hasKeysPropertiesIndexsNull();

                NumericAggregationStrategy strategy = NumericAggregationStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
                parentResult = strategy.evaluate(parent, results);
                parentResult.setKey(keyToUse);
                
                return parentResult;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return null;
    }
    
    //Operador where
    private static Boolean where(ValNode parent, ValNode operandChild, ValNode conditionChild) {
        List<ValResult> operandResults = new ArrayList<>();
        List<ValResult> conditionResults = new ArrayList<>();
        List<Map.Entry<String, String>> allConditions = new ArrayList<>();
        List<ValResult> parentResults = new ArrayList<>();
        boolean respectFilter = false;
        Integer conditionOperatorID = -1;
        String resultLog = "";
        String expression = "";

        try {
            if (conditionChild != null) {
                LOG.info("Execucao de um 'Where', no No " + parent.getNode().getNodeID());
                
                conditionOperatorID = conditionChild.getNode().getOperator().getOperatorID();

                if (OperationsUtils.resultsIsNotEmpty(conditionChild)) {
                    conditionResults = conditionChild.getResults();
                    expression = conditionChild.getResults().get(Constants.FIRSTRESULT).getExpression();
                    
                    //cria a lista de combinacoes propriedade-valor resultante do filho das condicoes
                    allConditions = conditionResults.stream().map(ValResult::getKey).filter(Objects::nonNull)
                            .map(ValKey::getDpmKeys).filter(Objects::nonNull)
                            .flatMap(map -> map.entrySet().stream())
                            .collect(Collectors.toList());

                    if (!allConditions.isEmpty() && operandChild != null && operandChild.getResults() != null && !operandChild.getResults().isEmpty()) {
                        respectFilter = false;
                        operandResults = operandChild.getResults();
                        parent.getNode().setFallbackValue(operandChild.getNode().getFallbackValue());

                        //vai verificar se os filhos respeitam ou nao as condicoes
                        for (ValResult operand : operandResults) {
                            if (operand != null) {
                                if(operand.valueIsNull()){
                                    operand = OperationsUtils.applyDefaultValue(operandChild, operand, operand.getDomain());
                                }

                                WhereStrategy strategy = AggregatorStrategyFactory.getStrategy(conditionOperatorID);
                                respectFilter = strategy.filterResultsUsingWhere(operand, allConditions);
                                if (respectFilter) {
                                    operand.setExpression(expressionBuilder.whereOperationBuilder(operandChild, operand, expression));
                                    operand.setDomain(domainBuilder.individualResultDomainBuilder(operand));
                                    parentResults.add(operand);
                                }
                            }
                        }
                        
                        //caso esteja vazio, inclui o default value
                        if(parentResults.isEmpty()){
                            //para obter o data type do valor como default
                            ValResult nullResult = operandResults.get(Constants.FIRSTRESULT);
                            nullResult.setKey(null);
                            
                            ValResult resultDefault = OperationsUtils.applyDefaultValue(operandChild, nullResult, null);
                            parentResults.add(resultDefault);
                        }
                        
                        parent.setResults(parentResults);
                        return true;
                    }
                }
                LOG.log(Level.SEVERE,"O No que trazia as condicoes ou os operandos do operador 'Where' veem vazios, no No " + parent.getNode().getNodeID());
                return false;
            } else {
                LOG.log(Level.SEVERE,"O No que trazia as condicoes do operador 'Where' vem null, no No " + parent.getNode().getNodeID());
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Where', no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }

    //Operador group by
    private static Boolean groupingClause(ValNode parent, List<ValNode> childs) {
        Map<String, String> groupingClauses = new HashMap<>();
        ValMLKey indexs = new ValMLKey();
        ValKey finalKey = new ValKey();
        ValResult childResult = new ValResult();
        String property = "";

        try {
            //Vai percorrer os filhos que contem as propriedades pela qual deve agrupar os valores
            LOG.info("Execucao de um 'Group By', no No " + parent.getNode().getNodeID());
            if (childs != null && !childs.isEmpty()) {
                for (ValNode child : childs) {
                    if (child.getResults() != null && !child.getResults().isEmpty()) {
                        childResult = child.getResults().get(Constants.FIRSTRESULT);
                        property = childResult.getRawValue();

                        switch (property) {
                            case Constants.PROPERTYROW:
                                indexs.setxIndex(0);
                                break;
                            case Constants.PROPERTYCOLUMN:
                                indexs.setyIndex(0);
                                break;
                            case Constants.PROPERTYSHEET:
                                indexs.setzIndex(0);
                                break;
                            default:
                                if (property != null && !property.isEmpty()) {
                                    groupingClauses.put(property, null);
                                }
                                break;

                        }
                    }
                }

                finalKey = new ValKey(indexs, groupingClauses);
                OperationsUtils.setOnlyOneResultToParent(parent, finalKey, null);
                parent.getResults().get(Constants.FIRSTRESULT).setExpression(expressionBuilder.groupByOperationBuilder(childs));

                return true;
            }

            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Group By', no No " + parent.getNode().getNodeID());
            
            return false;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Group By', no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }
    
    //Operador if then else
    private static Boolean ifThenElse(ValNode parent, ValNode conditionChild, ValNode thenChild, ValNode elseChild) {
        List<ValResult> ifResults = new ArrayList<>();
        List<ValResult> thenResults = new ArrayList<>();
        List<ValResult> elseResults = new ArrayList<>();
        boolean conditionHasKeys = false;
        List<ValResult> parentResults = new ArrayList<>();
        ValResult ifResult = new ValResult();
        ValResult thenResult = new ValResult();
        ValResult elseResult = new ValResult();

        String resultLog = "";

        Integer conditionNodeId = null;
        Integer thenNodeId = null;
        Integer elseNodeId = null;

        try {
            //vai buscar os results de cada um dos filhos 
            if (conditionChild != null && conditionChild.getResults() != null && !conditionChild.getResults().isEmpty()) {
                ifResults = conditionChild.getResults();
                conditionNodeId = conditionChild.getNode().getNodeID();
            }
            if (thenChild != null && thenChild.getResults() != null && !thenChild.getResults().isEmpty()) {
                thenResults = thenChild.getResults();
                thenNodeId = thenChild.getNode().getNodeID();
            }
            if (elseChild != null && elseChild.getResults() != null && !elseChild.getResults().isEmpty()) {
                elseResults = elseChild.getResults();
                elseNodeId = elseChild.getNode().getNodeID();
            }

            //avalia se é para usar chaves
            conditionHasKeys = OperationsUtils.isToUseKeys(conditionChild);
            if (conditionHasKeys) {
                for (ValResult ifResultFromResults : ifResults) {
                    //obtém os resultados
                    ifResult = ifResultFromResults;
                    ValKey resultKey = ifResult.getKey();

                    thenResult = (!thenResults.isEmpty()) ? thenResults.stream().filter(temp -> temp.getKey().equals(resultKey)).findFirst().orElse(null) : null;
                    elseResult = (!elseResults.isEmpty()) ? elseResults.stream().filter(temp -> temp.getKey().equals(resultKey)).findFirst().orElse(null) : null;

                    //aplica a operação
                    ValResult parentResult = applyIfThenElse(ifResult, thenResult, elseResult, resultKey);
                    parentResult.setExpression(expressionBuilder.ifThenElseOperationBuilder(conditionChild, thenChild, elseChild, ifResult, thenResult, elseResult));
                    parentResult.setDomain(domainBuilder.ifThenElseDomainBuilder(ifResult, thenResult, elseResult));
                    parentResults.add(parentResult);
                }
            } else {
                boolean thenHasKeys = OperationsUtils.hasKeys(thenChild);
                if (thenHasKeys && !conditionHasKeys && thenResults.size() >= 1 && ifResults.size() == 1) {
                    ValResult uniqueIfResult = ifResults.get(Constants.FIRSTRESULT);
                    for (int idx = 0; idx < thenResults.size(); idx++) {
                        thenResult = (thenResults != null && idx < thenResults.size()) ? thenResults.get(idx) : null;
                        elseResult = (elseResults != null && idx < elseResults.size()) ? elseResults.get(idx) : null;;
                        
                        ValResult parentResult = applyIfThenElse(uniqueIfResult, thenResult, elseResult, null);
                        parentResult.setExpression(expressionBuilder.ifThenElseOperationBuilder(conditionChild, thenChild, elseChild, ifResult, thenResult, elseResult));
                        parentResult.setDomain(domainBuilder.ifThenElseDomainBuilder(ifResult, thenResult, elseResult));
                        parentResults.add(parentResult);
                    }
                } else {
                    //obtém os resultados
                    ifResult = (!ifResults.isEmpty()) ? ifResults.get(Constants.FIRSTRESULT) : null;
                    thenResult = (!thenResults.isEmpty()) ? thenResults.get(Constants.FIRSTRESULT) : null;
                    elseResult = (!elseResults.isEmpty()) ? elseResults.get(Constants.FIRSTRESULT) : null;

                    //aplica a operação
                    ValResult parentResult = applyIfThenElse(ifResult, thenResult, elseResult, null);
                    parentResult.setExpression(expressionBuilder.ifThenElseOperationBuilder(conditionChild, thenChild, elseChild, ifResult, thenResult, elseResult));
                    parentResult.setDomain(domainBuilder.ifThenElseDomainBuilder(ifResult, thenResult, elseResult));
                    parentResults.add(parentResult);
                }
            }

            if (!parentResults.isEmpty()) {
                parent.setResults(parentResults);
                return true;
            } else {
                LOG.log(Level.SEVERE, "Não foi encontrados resultados para acrescentar no pai, no operador 'If Then Else', no nó " + parent.getNode().getNodeID());
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Ocorreu um erro no operador 'If Then Else', no nó " + parent.getNode().getNodeID() + " | ", e);
        }
        return false;
    }
    
    private static ValResult applyIfThenElse(ValResult ifResult, ValResult thenResult, ValResult elseResult, ValKey key){
        if (ifResult.valueIsNull() || ifResult.getRawValue().equals(Constants.FALSERESULT)) {
            if (elseResult == null || elseResult.valueIsNull()) {
                return new ValResult(key, new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEBOOLEAN), "true"));
            } else {
                return elseResult;
            }
        } else if (ifResult.getRawValue().equals(Constants.TRUERESULT)) {
            if (thenResult == null || thenResult.valueIsNull()) {
                return new ValResult(key, new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEBOOLEAN), null));
            } else {
                return thenResult;
            }
        }
        return null;
    }

    //Operador filter
    private static Boolean filter(ValNode parent, ValNode selectionChild, ValNode conditionChild) {
        List<ValResult> selectionResults = new ArrayList<>();
        List<ValResult> conditionResults = new ArrayList<>();
        List<ValResult> parentResults = new ArrayList<>();
        Map<ValResult, List<ValResult>> groupedValues = new HashMap<>();
        ValResult selectionResult = new ValResult();
        ValResult conditionResult = new ValResult();

        try {
            if (selectionChild != null && selectionChild.getResults() != null && !selectionChild.getResults().isEmpty()) {
                selectionResults = selectionChild.getResults();
            }
            if (conditionChild != null && conditionChild.getResults() != null && !conditionChild.getResults().isEmpty()) {
                conditionResults = conditionChild.getResults();
            }

            if (!conditionResults.isEmpty() && !selectionResults.isEmpty()) {
                Boolean isToUseKeys = OperationsUtils.isToUseKeys(selectionChild, conditionChild);
                if (isToUseKeys == null){
                    //DEU UM ERRO
                    return false;
                } else if (OperationsUtils.isToUseKeys(selectionChild, conditionChild)) {

                    ValKey innerJoinKey = OperationsUtils.defineInnerJoinKeys(selectionResults, conditionResults);

                    if (innerJoinKey == null || innerJoinKey.hasKeysPropertiesIndexsNull()) {
                        LOG.log(Level.SEVERE,"Nao foi encontrada uma 'inner key', no No " + parent.getNode().getNodeID());
                        return false;
                    }

                    //Agrupa os dados, simulando o "innerJoin"
                    groupedValues = OperationsUtils.groupValuesByInnerJoin(selectionResults, conditionResults,selectionChild, conditionChild,  innerJoinKey);
                } else {
                    groupedValues.put(selectionResults.get(Constants.FIRSTRESULT), conditionResults);
                }
                
                if (groupedValues.isEmpty()) {
                    LOG.log(Level.SEVERE,"Nao conseguiu agrupar os valores, no No " + parent.getNode().getNodeID());
                    return false;
                }
                
                //Comeca a percorrer os valores agrupados
                for (Map.Entry<ValResult, List<ValResult>> entry : groupedValues.entrySet()) {
                    //Se o da esquerda for null, usar o valor default
                    selectionResult = entry.getKey();

                    List<ValResult> conditionResultsForResult = entry.getValue();
                    if (conditionResultsForResult.size() == 1) {
                        if (selectionResult == null || selectionResult.valueIsNull()) {
                            selectionResult = OperationsUtils.applyDefaultValue(selectionChild, selectionResult, selectionResult != null ? selectionResult.getDomain() : null);
                        }

                        //Valores que partilham a mesma chave que o da esquerda
                        for (ValResult matchedValue : conditionResultsForResult) {

                            //Se o da direita for null, usar o valor default
                            conditionResult = matchedValue;
                            if (conditionResult == null || conditionResult.valueIsNull()) {
                                conditionResult = OperationsUtils.applyDefaultValue(conditionChild, conditionResult, conditionResult != null ? conditionResult.getDomain() : null);
                            }

                            //Aplica a operacao aos valores
                            
                            if ((selectionResult != null && !selectionResult.valueIsNull()) && Boolean.parseBoolean(conditionResult.getRawValue())) {
                                selectionResult.setExpression(expressionBuilder.filterOperationBuilder(selectionChild, conditionChild, selectionResult, conditionResult));
                                selectionResult.setDomain(domainBuilder.binaryOperationDomainBuilder(selectionResult, conditionResult));
                                parentResults.add(selectionResult);
                            }
                        }
                    } else {
                        LOG.log(Level.SEVERE,"No operador 'Filter' algum valor tem mais que uma condicao, no No " + parent.getNode().getNodeID());
                        return false;
                    }
                }
                
                //caso esteja vazio, inclui o default value
                if(parentResults.isEmpty()){
                    //para obter o data type do valor como default
                    ValResult nullResult = selectionResults.get(Constants.FIRSTRESULT);
                    nullResult.setKey(null);

                    ValResult resultDefault = OperationsUtils.applyDefaultValue(selectionChild, nullResult, null);
                    parentResults.add(resultDefault);
                }

                parent.setResults(parentResults);
                return true;
            } else {
                LOG.log(Level.SEVERE,"No operador 'Filter' os valores ou as condicoes vem a null ou vazios, no No " + parent.getNode().getNodeID());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Filter', no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }
    
    //operador element of
    private static Boolean elementOf(ValNode parent, ValNode operandChild, ValNode setChild){
        List<ValResult> operandResults = new ArrayList<>();
        List<String> setResults = new ArrayList<>();
        List<ValResult> parentResults = new ArrayList<>();
        
        String inLog = "in(";
        
        try {
            if(OperationsUtils.isChildOfWhere(parent)){
                return OperationsUtils.applyChildWhereLogic(parent, operandChild, setChild);
            }
                        
            if (operandChild != null && operandChild.getResults() != null && !operandChild.getResults().isEmpty()) {
                operandResults = operandChild.getResults();
            }
            if (setChild != null && setChild.getResults() != null && !setChild.getResults().isEmpty()) {
                setResults = setChild.getResults().stream().map(ValResult::getRawValue).collect(Collectors.toList());
                inLog += String.join(", ", setResults.stream().map(String::toString).collect(Collectors.toList()))+")";
            }
            
            if (!operandResults.isEmpty() && !setResults.isEmpty()) {
                for (ValResult result : operandResults) {
                    if (result != null) {
                        ValResult parentResult = new ValResult();
                        
                        if (!result.valueIsNull()) {
                            Boolean respectCondition = setResults.contains(result.getRawValue());
                            parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEBOOLEAN), respectCondition.toString()));
                        } else {
                            parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEBOOLEAN), Boolean.FALSE.toString()));
                        }
                        
                        parentResult.setExpression(expressionBuilder.elementOfOperationBuilder(result.getRawValue(), setResults));
                        parentResult.setDomain(domainBuilder.individualResultDomainBuilder(result));
                        parentResults.add(parentResult);
                        
                    }
                }
                parent.setResults(parentResults);
                return true;
            } else {
                LOG.log(Level.SEVERE,"No operador 'Element Of' os valores importados ou os possíveis vem a null ou vazios, no No " + parent.getNode().getNodeID());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Element Of', no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }
    
    //operador match
    private static Boolean matchCaracters(ValNode parent, ValNode operandChild, ValNode patternChild){
        List<ValResult> operandChilds = new ArrayList<>();
        String pattern = "";
        List<ValResult> parentResults = new ArrayList<>();
        
        try {
            if (operandChild != null && operandChild.getResults() != null && !operandChild.getResults().isEmpty()) {
                operandChilds = operandChild.getResults();
            }
            
            if (patternChild != null && patternChild.getNode().getScalar() != null && !patternChild.getNode().getScalar().isEmpty()) {
                pattern = patternChild.getNode().getScalar();
            }
            
            if(!operandChilds.isEmpty() && !pattern.isEmpty()){
                for(ValResult result : operandChilds){
                    String value = result.getRawValue();
                    
                    ValResult parentResult = new ValResult();
                    String booleanResult = "";
                    if(value == null){
                        booleanResult = null;
                    } else if(!value.isEmpty()){
                        Boolean match = value.matches(pattern);
                        booleanResult = match.toString();
                    } else {
                        booleanResult = "false";
                    }
                    
                    parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEBOOLEAN), booleanResult));
                    parentResult.setExpression(expressionBuilder.matchOperationBuilder(operandChild, result, pattern));
                    parentResult.setDomain(domainBuilder.individualResultDomainBuilder(result));
                    parentResults.add(parentResult);
                        
                }
                
                parent.setResults(parentResults);
                return true;
            } else {
                LOG.log(Level.SEVERE,"No operador 'Match' os valores ou o padrao vem a null ou vazios, no No " + parent.getNode().getNodeID());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Match', no No " + parent.getNode().getNodeID() + " | ", e);
        }
        
        return false;
    }

    //operador get
    private static Boolean get(ValNode parent, ValNode operandChild, ValNode componentChild) {
        List<ValResult> operandValues = new ArrayList<>();
        String component = "";
        List<ValResult> parentResults = new ArrayList<>();
        
        try {
            if (operandChild != null && operandChild.getResults() != null && !operandChild.getResults().isEmpty()) {
                operandValues = operandChild.getResults();
            }
            
            if(operandChild != null && operandChild.getResults() != null && !operandChild.getResults().isEmpty()){
                component = componentChild.getResults().get(Constants.FIRSTRESULT).getRawValue();
            }
            
            if(!operandValues.isEmpty() && !component.isEmpty()){
                for(ValResult result : operandValues){
                    String value = result.getRawValue();
                    if(value != null){
                        ValResult parentResult = new ValResult();
                        
                        if(component.equals(Constants.REFPERIOD)){
                            parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPEDATE), LocalDate.parse(result.getRefDate(), Constants.DATEFORMATUSEDBYVALIDATIONS).format(Constants.DATEFORMATUSEDBYVALIDATIONS)));
                        } else {
                            parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.DATATYPESTRINGNONEMPTY), result.getKey().getDpmKeys().get(component)));
                        }
                        
                        parentResult.setExpression(expressionBuilder.getOperationBuilder(operandChild, result, component));
                        parentResult.setDomain(domainBuilder.individualResultDomainBuilder(result));
                        parentResults.add(parentResult);
                    }
                        
                }
                
                parent.setResults(parentResults);
                return true;

            } else {
                LOG.log(Level.SEVERE,"No operador 'Get' os valores ou o component vem a null ou vazios, no No " + parent.getNode().getNodeID());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Get', no No " + parent.getNode().getNodeID() + " | ", e);
        }
        
        return false;
    }

    private static Boolean timeShift(ValNode parent, ValNode operandNode, ValNode periodNode, ValNode numberNode, ValNode dimensionNode, String entityId, String domain) {
        List<ValResult> parentResults = new ArrayList<>();
        String period = "";
        Integer numberPeriods = 0;
        String dimension = "";
        
        String refDateActual = "";
        LocalDate shiftDate = null;
        Integer nodeId = 0;
        Integer operationVId = 0;
        
        String resultLog = "";
        
        try {
            if(periodNode != null && periodNode.getNode().getScalar() != null && !periodNode.getNode().getScalar().isEmpty()){
                period = periodNode.getNode().getScalar();
            }
            if(numberNode != null && numberNode.getNode().getScalar() != null && !numberNode.getNode().getScalar().isEmpty()){
                numberPeriods = Integer.valueOf(numberNode.getNode().getScalar());
            }
            if(dimensionNode != null && dimensionNode.getResults() != null && !dimensionNode.getResults().isEmpty()){
                dimension = dimensionNode.getResults().get(Constants.FIRSTRESULT).getRawValue();
            }
            if(operandNode != null && operandNode.getResults() != null && !operandNode.getResults().isEmpty()){
                nodeId = operandNode.getNode().getNodeID();
                operationVId = operandNode.getNode().getOperationVersion().getOperationVID();
            }
            
            if (!period.isEmpty() && numberPeriods != 0 && nodeId != 0 && operationVId != 0) {
                if (dimension != null && !dimension.isEmpty() && dimension.equals(Constants.REFPERIOD)) {
                    refDateActual = operandNode.getResults().get(Constants.FIRSTRESULT).getRefDate();
                    LocalDate refDate = LocalDate.parse(refDateActual, Constants.DATEFORMATUSEDBYVALIDATIONS);

                    switch (period) {
                        case Constants.PERIODYEAR:
                            shiftDate = refDate.plusYears(numberPeriods);
                            break;
                        case Constants.PERIODSEMESTER:
                            //constants no 3 e 6
                            shiftDate = refDate.plusMonths(numberPeriods * 6);
                            break;
                        case Constants.PERIODQUARTER:
                            shiftDate = refDate.plusMonths(numberPeriods * 3);
                            break;
                        case Constants.PERIODMONTH:
                            shiftDate = refDate.plusMonths(numberPeriods);
                            break;
                        case Constants.PERIODWEEK:
                            shiftDate = refDate.plusWeeks(numberPeriods);
                            break;
                        case Constants.PERIODDAY:
                            shiftDate = refDate.plusDays(numberPeriods);
                            break;
                    }

                    if (shiftDate != null && entityId != null) {
                        String shiftDateString = shiftDate.format(Constants.DATEFORMATUSEDBYVALIDATIONS);
                        LocalDate auxTime = LocalDate.parse(shiftDateString,Constants.DATEFORMATISO8601);
                        
                        Map<Integer, List<ValResult>> resultsForRefDate = OperationsUtils.getResultsByNodeForTimeShift(operationVId, auxTime, entityId, nodeId, domain);
                        if (resultsForRefDate != null && !resultsForRefDate.isEmpty()) {
                            parentResults = resultsForRefDate.get(nodeId);
                            for(ValResult result : parentResults){
                                result.setExpression(expressionBuilder.timeShiftOperationBuilder(refDateActual, period, domain, "refPeriod"));
                                result.getDomain().addAll(operandNode.getResults().get(Constants.FIRSTRESULT).getDomain());
                            }
                        }
                    }
                    
                    if(!parentResults.isEmpty()){
                        parent.setResults(parentResults);
                        return true;
                    } else {
                        LOG.log(Level.SEVERE,"No operador 'Timeshift' nao encontrou resultados para o pai, no No " + parent.getNode().getNodeID());
                    }
                } else {
                    LOG.log(Level.SEVERE,"No operador 'Timeshift' a dimension e desconhecida, no No " + parent.getNode().getNodeID());
                }
            } else {
                LOG.log(Level.SEVERE,"No operador 'Timeshift' os valores ou os parametros vem a null ou vazios, no No " + parent.getNode().getNodeID());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Time shift', no No " + parent.getNode().getNodeID() + " | ", e);
        }
        
        return false;
    }

    private static Boolean parenthesis(ValNode parent, ValNode childNode) {
        String resultLog = "";
        
        try {
            if (childNode != null) {
                if (childNode.getNode().getScalar() != null) {
                    
                    parent.getNode().setScalar(childNode.getNode().getScalar());
                } else {
                    
                    childNode.getResults().stream().forEach(result -> result.setExpression(expressionBuilder.parenthesisOperationBuilder(childNode, result)));
                    parent.setResults(childNode.getResults());
                }

                return true;
            } else {
                LOG.log(Level.SEVERE,"No operador 'Parenthesis' o No " + parent.getNode().getNodeID() + " vem a null");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Parenthesis', no No " + parent.getNode().getNodeID() + " | ", e);
        }

        return false;
    }
}
