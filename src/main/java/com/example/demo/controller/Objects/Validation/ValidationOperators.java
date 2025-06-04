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
import com.example.demo.controller.Objects.Aggregation.*;
import com.example.demo.controller.Objects.Comparison.*;
import com.example.demo.controller.Objects.Entities.DPMOrigin.DataType;
import com.example.demo.controller.Objects.Entities.DPMOrigin.Operator;
import com.example.demo.controller.Objects.IndividualBoolean.*;
import com.example.demo.controller.Objects.Logical.*;
import com.example.demo.controller.Objects.Logs.LogValidationProcess;
import com.example.demo.controller.Objects.NumericAggregationStrategy.*;
import com.example.demo.controller.Objects.Where.*;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

            //subgrupo dos numéricos individuais
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

            //operadores de lógica
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
                
                
            //operadores com comportamentos próprios  
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

    //Preparacao para as operacoes binárias
    private static boolean evaluateBinaryOperations(ValNode parent, ValNode leftChild, ValNode rightChild, int operationsSubgroup) {
        List<ValResult> leftValues = new ArrayList<>();
        List<ValResult> rightValues = new ArrayList<>();
        boolean leftIsScalar = false;
        boolean rightIsScalar = false;
        boolean isToCompareToItem = false;

        try {
            LOG.info("Avaliacao dos casos para operacoes binárias, no nó " + parent.getNode().getNodeID());
            
            //Caso: Tem scalar
            leftIsScalar = leftChild.getNode().getScalar() != null;
            rightIsScalar = rightChild.getNode().getScalar() != null;
            isToCompareToItem = OperationsUtils.isToCompareWithItem(rightChild);
            
            if (OperationsUtils.isChildOfWhere(parent)) {
                LOG.info("Caso em que é filho de um where encontrado, no nó " + parent.getNode().getNodeID());
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
                
                LOG.info("Operacao binária aplicada sobre dois scalar, no nó " + parent.getNode().getNodeID());

                return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
            } else if (leftIsScalar) {
                ValValue scalarValue = new ValValue(leftChild.getNode().getScalar());
                ValResult scalarResult = new ValResult(scalarValue);

                //Criar a lista de scalar's com o mesmo tamanho que a outra lista.
                for (ValResult rightValue : rightChild.getResults()) {
                    leftValues.add(scalarResult);
                }
                leftChild.setResults(leftValues);
                
                LOG.info("Operacao binária aplicada com um scalar à esquerda, no nó " + parent.getNode().getNodeID());
                return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
            } else if (rightIsScalar) {
                ValValue scalarValue = new ValValue(rightChild.getNode().getScalar());
                ValResult scalarResult = new ValResult(scalarValue);

                //Criar a lista de scalar's com o mesmo tamanho que a outra lista.
                for (ValResult leftValue : leftChild.getResults()) {
                    rightValues.add(scalarResult);
                }
                rightChild.setResults(rightValues);
                
                LOG.info("Operacao binária aplicada com um scalar à direita, no nó " + parent.getNode().getNodeID());
                return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
            } else if (isToCompareToItem) {
                ValResult itemResult = rightChild.getResults().get(Constants.FIRSTRESULT);

                //Criar a lista de items com o mesmo tamanho que a outra lista.
                for (ValResult leftValue : leftChild.getResults()) {
                    rightValues.add(itemResult);
                }
                rightChild.setResults(rightValues);
                
                LOG.info("Operacao binária aplicada com um item à direita, no nó " + parent.getNode().getNodeID());
                return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
            }

            //Caso: Utlizar chaves
            Boolean isToUseKeys = OperationsUtils.isToUseKeys(leftChild, rightChild);
            if (isToUseKeys == null){
                //DEU UM ERRO
                return false;
            } else if (isToUseKeys) {
                LOG.info("Operacao binária aplicada com chaves, no nó " + parent.getNode().getNodeID());
                return prepareBinaryOperation(parent, leftChild, rightChild, true, operationsSubgroup);
            }

            //Caso: Normal, sem ser com scalar nem chaves
            LOG.info("Operacao binária aplicada sem chaves, no nó " + parent.getNode().getNodeID());
            return prepareBinaryOperation(parent, leftChild, rightChild, false, operationsSubgroup);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
            //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Avaliacao dos casos nas operacoes binárias com erros.", null, null, "Erro");
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
                LOG.info("Operacao numérica individual será aplicada sobre um scalar, no nó " + parent.getNode().getNodeID());
                ValValue scalarValue = new ValValue(operandChild.getNode().getScalar());
                ValResult scalarResult = new ValResult(scalarValue);
                operandValues.add(scalarResult);
                operandChild.setResults(operandValues);
            }

            return prepareIndividualOperations(parent, operandChild, operationsSubgroup);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Avaliacao dos casos nas operacoes de individuais com erros.", null, null, "Erro");
        }

        return false;
    }

    //Avaliacao das operacoes de agregacao
    private static boolean evaluateAggregateOperations(ValNode parent, ValNode operandChild, ValNode groupingClauseChild) {
        List<ValResult> operandValues = new ArrayList<>();
        List<ValResult> parentResults = new ArrayList<>();
        ValResult parentResult = new ValResult();
        String resultLog = "";

        try {
            if(operandChild != null && operandChild.getResults() != null && !operandChild.getResults().isEmpty()){
                operandValues = operandChild.getResults();
                
                if (groupingClauseChild != null && groupingClauseChild.getResults() != null) {
                    LOG.info("Operacao de agrupamento com chaves, no nó " + parent.getNode().getNodeID());
                    
                    //ir buscar a chave que agrupa os valores
                    ValResult resultFromGroup = groupingClauseChild.getResults().get(Constants.FIRSTRESULT);
                    ValKey key = resultFromGroup.getKey();

                    //agrupa os valores
                    Map<ValKey, List<ValResult>> valuesGroupedBy = OperationsUtils.groupValues(operandValues, key);
                    if(valuesGroupedBy == null || valuesGroupedBy.isEmpty()){
                        LOG.log(Level.SEVERE,"Ocorreu um erro no agrupamento dos valores, no nó " + parent.getNode().getNodeID());
//                        Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Avaliacao dos casos nas operacoes de agrupamento com erros.", null, null, "Erro");
                        return false;
                    }

                    //percorre os valores agrupados
                    for(ValKey keyThatGroupValues : valuesGroupedBy.keySet()){
                        List<ValResult> resultsThatShareKey = valuesGroupedBy.get(keyThatGroupValues);
                        
                        //resultLog  += parent.getNode().getOperator().getSymbol() + "("
                        //        +String.join(", ", resultsThatShareKey.stream().map(ValResult::getRawValue).collect(Collectors.toList()))+")";
                        //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, keyThatGroupValues.toString(), "Operacao");
                        
                        AggregationStrategy strategy = AggregationStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
                        parentResult = strategy.evaluate(operandChild, resultsThatShareKey);
                        parentResult.setKey(keyThatGroupValues);
                                                
                        //verifica se o resultado é nulo, pois os nulos devem ser ignorados
                        if (!parentResult.valueIsNull()) {
                            parentResult.setExpression(expressionBuilder.aggregateOperationBuilder(parent.getOperatorSymbol(), operandChild, groupingClauseChild, resultsThatShareKey, resultFromGroup));
                            parentResult.setDomain(domainBuilder.groupOfResultsDomainBuilder(resultsThatShareKey));
                            parentResults.add(parentResult);
                        } else {
                            //TODO: strategy deu null
                        }
                    }
                } else {
                    //caso que os dados não são agrupados
                    LOG.info("Operacao de agrupamento sem chaves, no nó " + parent.getNode().getNodeID());
                    
                    //resultLog += parent.getNode().getOperator().getSymbol() + "("
                    //        +String.join(", ", operandValues.stream().map(ValResult::getRawValue).collect(Collectors.toList()))+")";
                    //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, null, "Operacao");
                                        
                    AggregationStrategy strategy = AggregationStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
                    parentResult = strategy.evaluate(operandChild, operandValues);

                    //verifica se o resultado é nulo, pois os nulos devem ser ignorados
                    if (!parentResult.valueIsNull()) {
                        parentResult.setExpression(expressionBuilder.aggregateOperationBuilder(parent.getOperatorSymbol(), operandChild, null, operandValues, null));
                        parentResult.setDomain(domainBuilder.groupOfResultsDomainBuilder(operandValues));
                        parentResults.add(parentResult);
                    } else {
                        //TODO: strategy deu null
                    }
                }
                
                //Se o resultado dos pais for diferente de vazio, então adiciona os resultados no pai
                if(!parentResults.isEmpty()){
                    parent.setResults(parentResults);
                    return true;
                }

                //TODO: Deve dar falso???
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Avaliacao dos casos nas operacoes de agrupamento com erros.", null, null, "Erro");
        }

        return false;
    }
    
    private static Boolean evaluateAggregateNumericOperations(ValNode parent, List<ValNode> childs) {
        List<ValResult> values = new ArrayList();
        List<ValResult> parentResults = new ArrayList<>();
        
        try {
            if(childs != null && !childs.isEmpty()){
                childs.forEach(child -> {
                    if(child.getResults() == null && child.getNode().getScalar() != null){
                        values.add(new ValResult(new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), child.getNode().getScalar())));
                    }
                    if(child.getResults() != null && !child.getResults().isEmpty()){
                        values.addAll(child.getResults());
                    }
                });
                
                if(!values.isEmpty()){
                    if(OperationsUtils.isToUseKeys(childs)){
                        LOG.info("Operacao de agregacao de valores numéricos com chaves, no nó " + parent.getNode().getNodeID());
                        
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
                        LOG.info("Operacao de agregacao de valores numéricos sem chaves, no nó " + parent.getNode().getNodeID());
                        Map<ValKey, List<Map.Entry<ValNode, ValResult>>> results = OperationsUtils.groupValuesOfNodes(childs, null);
                                              
                        ValResult parentResult = applyNumericAggregateOperation(parent, results.get(null) , null);
                        parentResult.setExpression(expressionBuilder.aggregateNumericOperationBuilder(parent.getOperatorSymbol(), results.get(null)));
                        parentResult.setDomain(domainBuilder.aggregateNumericDomainBuilder(results.get(null)));
                        parentResults.add(parentResult);
                    }
                    
                    parent.setResults(parentResults);
                    return true;
                } else {
                    LOG.log(Level.SEVERE,"Não foram encontrados valores no filho do nó " + parent.getNode().getNodeID());
//                    Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes de agregacao de valores numéricos com erros.", null, null, "Erro");
                    return false;
                }
            } else {
                LOG.log(Level.SEVERE,"Não foram encontrados filhos do nó " + parent.getNode().getNodeID());
//                Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes de agregacao de valores numéricos com erros.", null, null, "Erro");
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes de agregacao de valores numéricos com erros.", null, null, "Erro");   
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

        try {
            operandValues = operandChild.getResults();

            //Vai percorrer a lista
            for (int i = 0; i < operandValues.size(); i++) {
                LOG.info("Preparacao dos dados para operacao individual, no nó " + parent.getNode().getNodeID());
                operandValue = operandValues.get(i);

                //Caso seja nulo, deve usar o defaultvalue
                if (operandValue == null || operandValue.valueIsNull()) {
                    operandValue = OperationsUtils.applyDefaultValue(operandChild, operandValue, operandValue != null ? operandValue.getDomain() : null);
                }

                keyToUse = operandValue.getKey();

                switch (operationsSubgroup) {
                    case Constants.INDIVIDUALNUMERICSUBGROUP:
                        parentResult = applyIndividualNumericOperation(parent, operandValue, keyToUse);
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
                    LOG.log(Level.SEVERE,"Ocorreu um erro num dos applyOperations, no nó " + parent.getNode().getNodeID());
//                    Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes individuais com erros.", null, null, "Erro");
                }
            }

            parent.setResults(resultsToParent);
            return true;

        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes individuais com erros.", null, null, "Erro");
        }

        return false;
    }

    //Consoante a utilizacao de chaves ou não, percorre os valores e envia-os para eles serem avaliados com operacoes numéricas
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
                LOG.info("Preparacao dos dados para operacao binária, com chaves no nó " + parent.getNode().getNodeID());
                
                ValKey innerJoinKey = OperationsUtils.defineInnerJoinKeys(leftValues, rightValues);

                if (innerJoinKey == null || innerJoinKey.hasKeysPropertiesIndexsNull()) {
                    LOG.log(Level.SEVERE,"Não foi encontrada uma 'inner key', no nó " + parent.getNode().getNodeID());
//                    Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes aritméticas com erros.", null, null, "Erro");
                    return false;
                }

                //Agrupa os dados, simulando o "innerJoin"
                groupedValues = OperationsUtils.groupValuesByInnerJoin(leftValues, rightValues, innerJoinKey);

                if (groupedValues.isEmpty()) {
                    LOG.log(Level.SEVERE,"Não conseguiu agrupar os valores, no nó " + parent.getNode().getNodeID());
//                    Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes aritméticas com erros.", null, null, "Erro");
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
                            LOG.log(Level.SEVERE,"Ocorreu um erro num dos applyOperations, no nó " + parent.getNode().getNodeID());
//                            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes aritméticas com erros.", null, null, "Erro");
                        }

                    }
                }
            } else {
                LOG.info("Operacao binária, sem chaves no nó " + parent.getNode().getNodeID());
                //Caso em que não se usa chaves (Ou são comparados dois valores apenas, ou são comparados valores com scalars)
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
                        LOG.log(Level.SEVERE,"Ocorreu um erro num dos applyOperations, no nó " + parent.getNode().getNodeID());
//                        Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes aritméticas com erros.", null, null, "Erro");
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
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes aritméticas com erros.", null, null, "Erro");
        }

        return false;
    }

    //Aplicacao das operacoes numéricas sobre os valores enviados como parâmetros
    private static ValResult applyNumericOperations(ValNode parent, ValNode leftNode, ValNode rightNode, ValResult leftValue, ValResult rightValue) {
        //FIXME - SEGUIR ABORDAGEM DAS STRATEGYS
        BigDecimal leftMargin = null;
        BigDecimal rightMargin = null;
        boolean isToUseIntervals = false;
        ValValue valueParentResult = new ValValue();
        ValResult parentResult = new ValResult();
        BigDecimal finalMargin = BigDecimal.ZERO;
        Integer precisionOfDivision = 0;
        ValKey keyToUse = new ValKey();
        String resultLog = "";
        String marginsLog = null;
        
        try {
            LOG.info("Aplicacao de uma operacao aritmética, no nó " + parent.getNode().getNodeID());
            precisionOfDivision = Integer.valueOf(Info.getInstance().getConfigValueByKey(Constants.PRECISIONOFDIVISION));

            //Caso sejam diferentes de nulo, constrói o big decimal com o valor, senão mantém a nulo
            BigDecimal left = (leftValue == null) ? null : (leftValue.getRawValue() == null) ? null : new BigDecimal(leftValue.getRawValue());
            BigDecimal right = (rightValue.getRawValue() == null) ? null : new BigDecimal(rightValue.getRawValue());

            //Caso algum seja nulo, o resultado deve ser nulo
            if (left == null || right == null) {
                LOG.info("Valor esquerdo ou direito vêem a null, no nó " + parent.getNode().getNodeID());
                
                keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);
                parentResult = new ValResult(keyToUse, null);
                
                //resultLog += left+ " "+ parent.getNode().getOperator().getSymbol() + " "+right;
                //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");
                
                return parentResult;
            } else {
                //Caso seja para utilizar margem de erro
                isToUseIntervals = OperationsUtils.isToUseMargin(leftNode, leftValue) || OperationsUtils.isToUseMargin(rightNode, rightValue);
                if (isToUseIntervals) {
                    LOG.info("Utilizada a margem, no nó " + parent.getNode().getNodeID());
                    
                    BigDecimal fixMargin = new BigDecimal(Info.getInstance().getConfigValueByKey(Constants.TOLERANCE));

                    //Caso estejam a zero (valor idêntico a null vindo da base de dados), deve ser para utilizar margem de erro parametrizada
                    leftMargin = OperationsUtils.setMarginValue(leftNode, leftValue);
                    rightMargin = OperationsUtils.setMarginValue(rightNode, rightValue);
                    
                    marginsLog = "Esquerda -> " + leftMargin.toPlainString() + ", Direita -> " +rightMargin.toPlainString();
                }
            }
            
            //resultLog = left+ " "+ parent.getNode().getOperator().getSymbol() + " "+right;
            
            switch (parent.getNode().getOperator().getOperatorID()) {
                case Constants.ADDITION:
                    if (isToUseIntervals) {
                        //Coloca o valor da margem no pai
                        if(leftMargin != null && rightMargin != null){
                            finalMargin = leftMargin.add(rightMargin);
                        } 
                    }

                    //Faz a operacao e coloca o resultado na lista de reusltados do pai
                    valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), left.add(right).toPlainString());
                    break;
                case Constants.SUBSTRACTION:
                    if (isToUseIntervals) {
                        //Coloca o valor da margem no pai
                        if(leftMargin != null && rightMargin != null){
                            finalMargin = leftMargin.add(rightMargin);
                        } 
                    }

                    //Faz a operacao e coloca o resultado na lista de reusltados do pai
                    valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), left.subtract(right).toPlainString());
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
                        //Faz a operacao
                        if (right == BigDecimal.ZERO) {
                            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID());
//                            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacao de divisão com zero no denominador.", null, null, "Erro");
                        } else {
                            valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), left.divide(right, precisionOfDivision, RoundingMode.HALF_UP).toPlainString());
                        }
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//                        Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacao de divisão com erros.", null, null, "Erro");
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

                    //Faz a operacao e coloca o resultado na lista de reusltados do pai
                    valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), left.multiply(right).toPlainString());
                    break;
                default:
                    //Operador desconhecido
                    throw new AssertionError();
            }

            //constrói a chave
            keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);
            //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, marginsLog, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");

            //adiciona o resultado ao pai
            parentResult = new ValResult(keyToUse, valueParentResult, finalMargin);
            return parentResult;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes aritméticas com erros.", null, null, "Erro");
        }

        return null;
    }

    //Aplicacao das operacoes de comparacao sobre os valores enviados como parâmetros
    private static ValResult applyComparisonOperations(ValNode parent, ValNode leftNode, ValNode rightNode, ValResult leftValue, ValResult rightValue) {
        boolean isToUseIntervals = false;
        BigDecimal leftMargin = null;
        BigDecimal rightMargin = null;
        DataType valuesDataType = new DataType();
        Object left = null;
        Object right = null;
        ValValue valueParentResult = new ValValue();
        ValResult parentResult = new ValResult();
        ValKey keyToUse = new ValKey();

        boolean leftIsScalar = false;
        boolean rightIsScalar = false;
        
        String resultLog = "";
        String marginLog = "";

        try {
            LOG.info("Aplicacao de uma operacao de comparacao, no nó " + parent.getNode().getNodeID());
            
            leftIsScalar = leftNode.getNode().getScalar() != null;
            rightIsScalar = rightNode.getNode().getScalar() != null;

            if (leftValue.getResult() == null || leftValue.valueIsNull() || rightValue.getResult() == null || rightValue.valueIsNull()) {
                LOG.info("Valor esquerdo ou direito vêem a null, no nó " + parent.getNode().getNodeID());
                
                keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);
                parentResult = new ValResult(keyToUse, null);
                
                //resultLog = leftValue + " " + parent.getNode().getOperator().getSymbol() + " " +rightValue;
                //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");
                
                return parentResult;
            }

            valuesDataType = OperationsUtils.determineDataType(leftValue, rightValue, leftIsScalar, rightIsScalar, leftNode);
            left = OperationsUtils.transformValue(leftValue, valuesDataType, leftNode);

            valuesDataType = OperationsUtils.determineDataType(leftValue, rightValue, leftIsScalar, rightIsScalar, rightNode);
            right = OperationsUtils.transformValue(rightValue, valuesDataType, rightNode);

            isToUseIntervals = leftNode.getNode().isUseIntervalArithmetics() || 
                                rightNode.getNode().isUseIntervalArithmetics() ||
                                (leftValue.getMargin() != null && leftValue.getMargin() != BigDecimal.ZERO) ||
                                (rightValue.getMargin() != null && rightValue.getMargin() != BigDecimal.ZERO);
            if (isToUseIntervals) {
                LOG.info("Utilizada a margem, no nó " + parent.getNode().getNodeID());
                
                leftMargin = OperationsUtils.setMarginValue(leftNode, leftValue);
                rightMargin = OperationsUtils.setMarginValue(rightNode, rightValue);
                
                marginLog = "Esquerda -> " + leftMargin + ", Direita -> " +rightMargin;
            }

            //resultLog = left + " " + parent.getNode().getOperator().getSymbol() + " " +right;       
            
            ComparisonStrategy strategy = ComparisonStrategyFactory.getStrategy(valuesDataType.getDataTypeId(), parent.getNode().getOperator().getOperatorID());
            parentResult = strategy.compare(left, right, isToUseIntervals, leftMargin, rightMargin);

            //constrói a chave
            keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);
            //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, marginLog, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");
            
            //adiciona o resultado ao pai
            parentResult.setKey(keyToUse);
            return parentResult;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes de comparacao com erros.", null, null, "Erro");
        }

        return null;
    }

    //Aplicacao das operacoes de lógicas sobre os valores enviados como parâmetros
    private static ValResult applyLogicalOperations(ValNode parent, ValNode leftNode, ValNode rightNode, ValResult leftValue, ValResult rightValue) {
        Boolean left = null;
        Boolean right = null;
        ValValue valueParentResult = new ValValue();
        ValResult parentResult = new ValResult();
        ValKey keyToUse = new ValKey();
        
        String resultLog = "";

        try {
            LOG.info("Aplicacao de uma operacao lógica, no nó " + parent.getNode().getNodeID());
            
            if (leftValue.getResult() == null || leftValue.valueIsNull() || rightValue.getResult() == null || rightValue.valueIsNull()) {
                LOG.info("Valor esquerdo ou direito vêem a null, no nó " + parent.getNode().getNodeID());
                
                keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);
                parentResult = new ValResult(keyToUse, null);
                
                //resultLog = leftValue + "(NodeId:"+leftNode.getNode().getNodeID() + ") " + parent.getNode().getOperator().getSymbol() + " " +rightValue + "(NodeId:"+rightNode.getNode().getNodeID()+")";
                //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");
                
                return parentResult;
            }

            left = Boolean.valueOf(leftValue.getRawValue());
            right = Boolean.valueOf(rightValue.getRawValue());

            //resultLog = left + "(NodeId:"+leftNode.getNode().getNodeID() + ") " + parent.getNode().getOperator().getSymbol() + " " +right + "(NodeId:"+rightNode.getNode().getNodeID()+")";
            
            LogicalStrategy strategy = LogicalStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
            Boolean result = strategy.compare(left, right);

            valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), (result != null) ? result.toString() : null);

            //constrói a chave
            keyToUse = OperationsUtils.buildSharedKey(leftValue, rightValue);
            //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");
            
            //adiciona o resultado ao pai
            parentResult = new ValResult(keyToUse, valueParentResult);
            return parentResult;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes de lógica com erros.", null, null, "Erro");
        }

        return null;
    }

    //Aplicacao das operacoes sobre operandos individuais que retornam booleanos
    private static ValResult applyIndividualBooleanOperation(ValNode parent, ValResult operandValue, ValKey keyToUse) {
        String operand = null;
        ValValue valueParentResult = new ValValue();
        ValResult parentResult = new ValResult();
        Boolean result = null;
        String resultLog = "";
        
        try {
            LOG.info("Aplicacao de uma operacao booleana individual, no nó " + parent.getNode().getNodeID());
            
            operand = operandValue.getRawValue();

            //resultLog = parent.getNode().getOperator().getSymbol() + " " +operand;
            //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");
            
            IndividualBooleanStrategy strategy = IndividualBooleanStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
            result = strategy.evaluate(operand);

            valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), (result != null) ? result.toString() : null);
            parentResult = new ValResult(keyToUse, valueParentResult);
            return parentResult;
        
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes booleanos individuais com erros.", null, null, "Erro");
        }

        return null;
    }

    //Aplicacao das operacoes sobre operandos individuais que retornam numéricos
    private static ValResult applyIndividualNumericOperation(ValNode parent, ValResult operandValue, ValKey keyToUse) {
        String resultLog = "";
        ValValue valueParentResult = new ValValue();

        try {
            LOG.info("Aplicacao de uma operacao numérica individual, no nó " + parent.getNode().getNodeID());
            
            //Caso sejam diferentes de nulo, constrói o big decimal com o valor, senão mantém a nulo
            BigDecimal operand = (operandValue.getRawValue() == null) ? null : new BigDecimal(operandValue.getRawValue());

            //Caso algum seja nulo, o resultado deve ser nulo
            resultLog = parent.getNode().getOperator().getSymbol() + " " +operand;
            if (operand == null) {
                LOG.info("Operando a null', no nó " + parent.getNode().getNodeID());
                //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");
                return new ValResult(keyToUse, null);
            }
            

            switch (parent.getNode().getOperator().getOperatorID()) {
                //Faz a operacao e coloca o resultado na lista de reusltados do pai
                case Constants.UNARYPLUS:
                    valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), operand.toPlainString());

                    break;
                case Constants.ABSOLUTEVALUE:
                    valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), operand.abs().toPlainString());

                    break;
                case Constants.UNARYMINUS:
                    valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), operand.negate().toPlainString());

                    break;
                case Constants.SQUAREROOT:
                    double result = Math.sqrt(operand.doubleValue());
                    valueParentResult = new ValValue(OperationsUtils.getDataTypeByID(Constants.DECIMAL), result + "");

                    break;
                default:
                    //Operador desconhecido.
                    throw new AssertionError();
            }
            
            //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (keyToUse != null) ? keyToUse.toString() : null, "Operacao");
            //adiciona o resultado ao pai
            return new ValResult(keyToUse, valueParentResult);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacoes numéricas individuais com erros.", null, null, "Erro");
        }

        return null;

    }
    
    private static ValResult applyNumericAggregateOperation(ValNode parent, List<Map.Entry<ValNode, ValResult>> results, ValKey keyToUse) {
        String resultLog = "";
        boolean isToUseKeys = false;
        ValResult parentResult = new ValResult();

        try {
            if (results != null && !results.isEmpty()) {
                isToUseKeys = keyToUse != null && !keyToUse.hasKeysPropertiesIndexsNull();

                //resultLog += parent.getNode().getOperator().getSymbol() + "("
                //        + String.join(", ", results.stream().map(Pair::getValue).map(ValResult::getRawValue).collect(Collectors.toList())) + ")";
                //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (isToUseKeys) ? keyToUse.toString() : null, "Operacao");

                NumericAggregationStrategy strategy = NumericAggregationStrategyFactory.getStrategy(parent.getNode().getOperator().getOperatorID());
                parentResult = strategy.evaluate(parent, results);
                parentResult.setKey(keyToUse);
                
                return parentResult;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Avaliacao dos casos nas operacoes de agrupamento com erros.", null, null, "Erro");
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
                LOG.info("Execucao de um 'Where', no nó " + parent.getNode().getNodeID());
                
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

                        //vai verificar se os filhos respeitam ou não as condicoes
                        for (ValResult operand : operandResults) {
                            if (operand != null) {
                                if(operand.valueIsNull()){
                                    operand = OperationsUtils.applyDefaultValue(operandChild, operand, operand.getDomain());
                                }
                                //resultLog = "Operando: " + operand.getRawValue() + " | Condicoes resultantes do nó: " + conditionChild.getNode().getNodeID();
                                //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, operand.getKey() != null ? operand.getKey().toString() : null, "Operacao");

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
                LOG.log(Level.SEVERE,"O nó que trazia as condicoes ou os operandos do operador 'Where' vêem vazios, no nó " + parent.getNode().getNodeID());
                return false;
            } else {
                LOG.log(Level.SEVERE,"O nó que trazia as condicoes do operador 'Where' vem null, no nó " + parent.getNode().getNodeID());
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Where', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Where com erros.", null, null, "Erro");
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
            //Vai percorrer os filhos que contém as propriedades pela qual deve agrupar os valores
            LOG.info("Execucao de um 'Group By', no nó " + parent.getNode().getNodeID());
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

                            //TODO: Caso se aparecer algo que não entre na condicao?
                        }
                    }
                }

                finalKey = new ValKey(indexs, groupingClauses);
                OperationsUtils.setOnlyOneResultToParent(parent, finalKey, null);
                parent.getResults().get(Constants.FIRSTRESULT).setExpression(expressionBuilder.groupByOperationBuilder(childs));

                return true;
            }

            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Group By', no nó " + parent.getNode().getNodeID());
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Group By sem filhos para agrupar.", null, null, "Erro");
            
            return false;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Group By', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Group By com erros.", null, null, "Erro");
        }

        return false;
    }
    
    //Operador if then else
    private static Boolean ifThenElse(ValNode parent, ValNode conditionChild, ValNode thenChild, ValNode elseChild){
        List<ValResult> ifResults = new ArrayList<>();
        List<ValResult> thenResults = new ArrayList<>();
        List<ValResult> elseResults = new ArrayList<>();
        boolean isToUseKeys = false;
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
            if(conditionChild != null && conditionChild.getResults() != null && !conditionChild.getResults().isEmpty()){
                ifResults = conditionChild.getResults();
                conditionNodeId = conditionChild.getNode().getNodeID();
            }
            if(thenChild != null && thenChild.getResults() != null && !thenChild.getResults().isEmpty()){
                thenResults = thenChild.getResults();
                thenNodeId = thenChild.getNode().getNodeID();
            }
            if(elseChild != null && elseChild.getResults() != null && !elseChild.getResults().isEmpty()){
                elseResults = elseChild.getResults();
                elseNodeId = elseChild.getNode().getNodeID();
            }
            
            //avalia se é para usar chaves
            isToUseKeys = OperationsUtils.isToUseKeys(conditionChild);
            if(isToUseKeys){
                LOG.info("Execucao de um 'If Then Else' com chaves, no nó " + parent.getNode().getNodeID());
                
                for (ValResult ifResultFromResults : ifResults) {
                    //obtém os resultados
                    ifResult = ifResultFromResults;
                    ValKey resultKey = ifResult.getKey();

                    thenResult = (!thenResults.isEmpty()) ? thenResults.stream().filter(temp -> temp.getKey().equals(resultKey)).findFirst().orElse(null) : null;
                    elseResult = (!elseResults.isEmpty()) ? elseResults.stream().filter(temp -> temp.getKey().equals(resultKey)).findFirst().orElse(null) : null;

                    //aplica a operacao
                    ValResult parentResult = applyIfThenElse(ifResult, thenResult, elseResult, resultKey);
                    parentResult.setExpression(expressionBuilder.ifThenElseOperationBuilder(conditionChild, thenChild, elseChild, ifResult, thenResult, elseResult));
                    parentResult.setDomain(domainBuilder.ifThenElseDomainBuilder(ifResult, thenResult, elseResult));
                    parentResults.add(parentResult);

                    //resultLog = OperationsUtils.generateIfThenElseLog(ifResult, thenResult, elseResult, conditionNodeId, thenNodeId, elseNodeId);
                    //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, (resultKey != null) ? resultKey.toString() : null, "Operacao");
                }
            } else {
                LOG.info("Execucao de um 'If Then Else' sem chaves, no nó " + parent.getNode().getNodeID());
                
                //obtém os resultados
                ifResult = (!ifResults.isEmpty()) ? ifResults.get(Constants.FIRSTRESULT) : null;
                thenResult = (!thenResults.isEmpty()) ? thenResults.get(Constants.FIRSTRESULT) : null;
                elseResult = (!elseResults.isEmpty()) ? elseResults.get(Constants.FIRSTRESULT) : null;
                
                //aplica a operacao
                ValResult parentResult = applyIfThenElse(ifResult, thenResult, elseResult, null);
                parentResult.setExpression(expressionBuilder.ifThenElseOperationBuilder(conditionChild, thenChild, elseChild, ifResult, thenResult, elseResult));
                parentResult.setDomain(domainBuilder.ifThenElseDomainBuilder(ifResult, thenResult, elseResult));
                parentResults.add(parentResult);

                //resultLog = OperationsUtils.generateIfThenElseLog(ifResult, thenResult, elseResult, conditionNodeId, thenNodeId, elseNodeId);
                //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, null, "Operacao");
            }
            
            if(!parentResults.isEmpty()){
                parent.setResults(parentResults);
                return true;
            } else {
                LOG.log(Level.SEVERE,"Não foi encontrados resultados para acrescentar no pai, no operador 'If Then Else', no nó " + parent.getNode().getNodeID());
                return false;
            }
        } catch (Exception e){
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'If Then Else', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "If then else com erros.", null, null, "Erro");
        }
        return false;
    }
    
    private static ValResult applyIfThenElse(ValResult ifResult, ValResult thenResult, ValResult elseResult, ValKey key){
        if (ifResult.valueIsNull() || ifResult.getRawValue().equals(Constants.FALSERESULT)) {
            if (elseResult == null || elseResult.valueIsNull()) {
                return new ValResult(key, new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), null));
            } else {
                return elseResult;
            }
        } else if (ifResult.getRawValue().equals(Constants.TRUERESULT)) {
            if (thenResult == null || thenResult.valueIsNull()) {
                return new ValResult(key, new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), null));
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
        String resultLog = "";
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
                    LOG.info("Agrupamento de dados para operador 'Filter', usando lógica da 'innerJoin' no nó " + parent.getNode().getNodeID());

                    ValKey innerJoinKey = OperationsUtils.defineInnerJoinKeys(selectionResults, conditionResults);

                    if (innerJoinKey == null || innerJoinKey.hasKeysPropertiesIndexsNull()) {
                        LOG.log(Level.SEVERE,"Não foi encontrada uma 'inner key', no nó " + parent.getNode().getNodeID());
//                        Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacao 'Filter' com erros.", null, null, "Erro");
                        Info.getInstance().getValidationsLogs().add(new LogValidationProcess("Estão em falta valores importados para a operacao " + parent.getNode().getOperationVersion().getOperation().getCode() + ".", LocalDateTime.MAX));
                        return false;
                    }

                    //Agrupa os dados, simulando o "innerJoin"
                    groupedValues = OperationsUtils.groupValuesByInnerJoin(selectionResults, conditionResults, innerJoinKey);
                } else {
                    groupedValues.put(selectionResults.get(Constants.FIRSTRESULT), conditionResults);
                }
                
                if (groupedValues.isEmpty()) {
                    LOG.log(Level.SEVERE,"Não conseguiu agrupar os valores, no nó " + parent.getNode().getNodeID());
//                    Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Operacao 'Filter' com erros.", null, null, "Erro");
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
                            //resultLog = "Operando: "+selectionResult.getRawValue() + " | Condicao: " + conditionResult.getRawValue();
                            //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, selectionResult.getKey() != null ? selectionResult.getKey().toString() : null, "Operacao");
                            
                            if (Boolean.parseBoolean(conditionResult.getRawValue())) {
                                selectionResult.setExpression(expressionBuilder.filterOperationBuilder(selectionChild, conditionChild, selectionResult, conditionResult));
                                selectionResult.setDomain(domainBuilder.binaryOperationDomainBuilder(selectionResult, conditionResult));
                                parentResults.add(selectionResult);
                            }
                        }
                    } else {
                        LOG.log(Level.SEVERE,"No operador 'Filter' algum valor tem mais que uma condicao, no nó " + parent.getNode().getNodeID());
//                        Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Filter com erros.", null, null, "Erro");
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
                LOG.log(Level.SEVERE,"No operador 'Filter' os valores ou as condicoes vem a null ou vazios, no nó " + parent.getNode().getNodeID());
//                Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Filter com erros.", null, null, "Erro");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Filter', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Filter com erros.", null, null, "Erro");
        }

        return false;
    }
    
    //operador element of
    private static Boolean elementOf(ValNode parent, ValNode operandChild, ValNode setChild){
        List<ValResult> operandResults = new ArrayList<>();
        List<String> setResults = new ArrayList<>();
        List<ValResult> parentResults = new ArrayList<>();
        
        String inLog = "in(";
        String resultLog = "";
        
        try {
            if(OperationsUtils.isChildOfWhere(parent)){
                LOG.info("Caso em que é filho de um where encontrado, no nó " + parent.getNode().getNodeID());
                return OperationsUtils.applyChildWhereLogic(parent, operandChild, setChild);
            }
            
            LOG.info("Apliacao de operacao 'Element Of' no nó " + parent.getNode().getNodeID());
            
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
                            parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), respectCondition.toString()));
                        } else {
                            parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), Boolean.FALSE.toString()));
                        }
                        
                        parentResult.setExpression(expressionBuilder.elementOfOperationBuilder(result.getRawValue(), setResults));
                        parentResult.setDomain(domainBuilder.individualResultDomainBuilder(result));
                        parentResults.add(parentResult);
                        
                        //resultLog = result.getRawValue() + " " + inLog;
                        //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, result.getKey() != null ? result.getKey().toString() : null, "Operacao");
                    }
                }
                parent.setResults(parentResults);
                return true;
            } else {
                LOG.log(Level.SEVERE,"No operador 'Element Of' os valores importados ou os possíveis vem a null ou vazios, no nó " + parent.getNode().getNodeID());
//                Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Element Of com erros.", null, null, "Erro");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Element Of', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Element Of com erros.", null, null, "Erro");
        }

        return false;
    }
    
    //operador match
    private static Boolean matchCaracters(ValNode parent, ValNode operandChild, ValNode patternChild){
        List<ValResult> operandChilds = new ArrayList<>();
        String pattern = "";
        List<ValResult> parentResults = new ArrayList<>();
        String resultLog = "";
        
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
                    
                    parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.BOOLEAN), booleanResult));
                    parentResult.setExpression(expressionBuilder.matchOperationBuilder(operandChild, result, pattern));
                    parentResult.setDomain(domainBuilder.individualResultDomainBuilder(result));
                    parentResults.add(parentResult);
                        
                    //resultLog = result.getRawValue() + " match(" + pattern +")";
                    //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, result.getKey() != null ? result.getKey().toString() : null, "Operacao"); 
                }
                
                parent.setResults(parentResults);
                return true;
            } else {
                LOG.log(Level.SEVERE,"No operador 'Match' os valores ou o padrão vem a null ou vazios, no nó " + parent.getNode().getNodeID());
//                Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Match com erros.", null, null, "Erro");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Match', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Match com erros.", null, null, "Erro");
        }
        
        return false;
    }

    //operador get
    private static Boolean get(ValNode parent, ValNode operandChild, ValNode componentChild) {
        List<ValResult> operandValues = new ArrayList<>();
        String component = "";
        List<ValResult> parentResults = new ArrayList<>();
        String resultLog = "";
        
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
                            parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.DATE), LocalDate.parse(result.getRefDate(), Constants.DATEFORMATUSEDBYVALIDATIONS).format(Constants.DATEFORMATUSEDBYVALIDATIONS)));
                        } else {
                            parentResult = new ValResult(result.getKey(), new ValValue(OperationsUtils.getDataTypeByID(Constants.STRINGNONEMPTY), result.getKey().getDpmKeys().get(component)));
                        }
                        
                        parentResult.setExpression(expressionBuilder.getOperationBuilder(operandChild, result, component));
                        parentResult.setDomain(domainBuilder.individualResultDomainBuilder(result));
                        parentResults.add(parentResult);
                    }
                        
                    //resultLog = result.getRawValue() + " get (" + component +")";
                    //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, result.getKey() != null ? result.getKey().toString() : null, "Operacao"); 
                }
                
                parent.setResults(parentResults);
                return true;

            } else {
                LOG.log(Level.SEVERE,"No operador 'Get' os valores ou o component vem a null ou vazios, no nó " + parent.getNode().getNodeID());
//                Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Get com erros.", null, null, "Erro");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Get', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Get com erros.", null, null, "Erro");
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
                        //resultLog = "TimeShift("+refDateActual+", "+period+", "+numberPeriods+", refPeriod)";
                        //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, null, "Operacao"); 
                        
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
                        LOG.log(Level.SEVERE,"No operador 'Timeshift' não encontrou resultados para o pai, no nó " + parent.getNode().getNodeID());
//                        Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Time shift com erros.", null, null, "Erro");
                    }
                } else {
                    LOG.log(Level.SEVERE,"No operador 'Timeshift' a dimension é desconhecida, no nó " + parent.getNode().getNodeID());
//                    Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Time shift com erros.", null, null, "Erro");
                }
            } else {
                LOG.log(Level.SEVERE,"No operador 'Timeshift' os valores ou os parametros vem a null ou vazios, no nó " + parent.getNode().getNodeID());
//                Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Time shift com erros.", null, null, "Erro");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Time shift', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Time shift com erros.", null, null, "Erro");
        }
        
        return false;
    }

    private static Boolean parenthesis(ValNode parent, ValNode childNode) {
        String resultLog = "";
        
        try {
            if (childNode != null) {
                if (childNode.getNode().getScalar() != null) {
                    //resultLog = "(Scalar: "+childNode.getNode().getScalar()+")";
                    //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, null, "Operacao"); 
                    
                    parent.getNode().setScalar(childNode.getNode().getScalar());
                } else {
                    //resultLog = "("+String.join(", ", childNode.getResults().stream().map(ValResult::getRawValue).collect(Collectors.toList()))+")";
                    //Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), resultLog, null, null, "Operacao"); 
                    
                    childNode.getResults().stream().forEach(result -> result.setExpression(expressionBuilder.parenthesisOperationBuilder(childNode, result)));
                    parent.setResults(childNode.getResults());
                }

                return true;
            } else {
                LOG.log(Level.SEVERE,"No operador 'Parenthesis' o nó " + parent.getNode().getNodeID() + " vem a null");
//                Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Parenthesis com erros.", null, null, "Erro");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Ocorreu um erro no operador 'Parenthesis', no nó " + parent.getNode().getNodeID() + " | ", e);
//            Utils.addLogOfOperations(parent.getNode().getOperationVersion().getOperationVID(), parent.getNode().getNodeID(), "Parenthesis com erros.", null, null, "Erro");
        }

        return false;
    }
}
