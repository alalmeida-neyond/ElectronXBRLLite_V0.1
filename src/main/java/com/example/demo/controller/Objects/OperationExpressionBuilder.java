/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.example.demo.controller.Objects.Entities.Operator;

import java.util.Map;
import java.util.AbstractMap;
import java.util.logging.Logger;

/**
 *
 * @author njesus
 */
public class OperationExpressionBuilder {
    
    private final static Logger LOG = Logger.getLogger(OperationExpressionBuilder.class.getName());
    
    public String binaryOperationBuilder(String symbol, ValNode left, ValNode right, ValResult leftResult, ValResult rightResult){
        StringBuilder sb = new StringBuilder();
        
        try {
            String leftString = getValue(left, leftResult);
            String rightString = getValue(right, rightResult);

            sb.append(leftString).append(" ").append(symbol).append(" ").append(rightString);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão binária. | ", e);
        }
        
        return sb.toString();
    }
    
    public String individualOperationBuilder(String symbol, ValNode operand, ValResult operandResult){
        StringBuilder sb = new StringBuilder();
        
        try {
            String operandString = getValue(operand, operandResult);

            sb.append(symbol).append(" ").append(operandString);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão individual. | ", e);
        }
        
        return sb.toString();
    }
    
    public String aggregateOperationBuilder(String symbol, ValNode operand, ValNode grouping, List<ValResult> operandResults, ValResult groupingResult){
        StringBuilder sb = new StringBuilder();
        
        try {
            String operandString = joinOperandResults(operand, operandResults);
            String groupingString = getValue(grouping, groupingResult);

            sb.append(symbol).append(" (").append(operandString).append(") ").append(groupingString);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão de agregacao. | ", e);
        }
        
        return sb.toString();
    }
    
    public String aggregateNumericOperationBuilder(String symbol, List<Map.Entry<ValNode, ValResult>> operands){
        StringBuilder sb = new StringBuilder();
        
        try {
            String operandString = joinOperandResultsByPairs(operands);

            sb.append(symbol).append(" (").append(operandString).append(")");
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão de agregacao numérica. | ", e);
        }
        
        return sb.toString();
    }
    
    public String parenthesisOperationBuilder(ValNode operand, ValResult operandResult){
        StringBuilder sb = new StringBuilder();
        
        try {
            String operandString = getValue(operand, operandResult);

            sb.append("(").append(operandString).append(")");
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão do operador \"Parenthesis\". | ", e);
        }
        
        return sb.toString();
    }
    
    public String matchOperationBuilder(ValNode operandNode, ValResult result, String pattern){
        StringBuilder sb = new StringBuilder();
        
        try {
            String operandString = getValue(operandNode, result);

            sb.append("match (").append(operandString).append(", \"").append(pattern).append("\")");
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão do operador \"Match\". | ", e);
        }
        
        return sb.toString();
    }
    
    public String getOperationBuilder(ValNode operandNode, ValResult result, String property){
        StringBuilder sb = new StringBuilder();
        
        try {
            String operandString = getValue(operandNode, result);

            sb.append("get (").append(operandString).append(", \"").append(property).append("\")");
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão do operador \"Get\". | ", e);
        }
        
        return sb.toString();
    }
    
    public String childWhereOperationBuilder(ValNode parentNode, ValNode leftNode, ValNode rightNode){
        StringBuilder sb = new StringBuilder();
        Operator op = new Operator();
        List<ValResult> left = new ArrayList<>();
        List<ValResult> right = new ArrayList<>();
        
        try {
            if(OperationsUtils.nodeIsNotNull(parentNode)){
                op = parentNode.getNode().getOperator();
                
                if(OperationsUtils.nodeIsNotNull(leftNode) && OperationsUtils.resultsIsNotEmpty(leftNode)) left = leftNode.getResults();
                if(OperationsUtils.nodeIsNotNull(rightNode) && OperationsUtils.resultsIsNotEmpty(rightNode)) right = rightNode.getResults();
            
                switch(op.getOperatorID()){
                    case Constants.EQUALSTO:
                    case Constants.NOTEQUALTO:
                        if(!left.isEmpty() && left.size() == 1 && !right.isEmpty() && right.size() == 1){
                            sb.append(getValue(leftNode, left.get(Constants.FIRSTRESULT)));
                            sb.append(" ").append(op.getSymbol()).append(" ");
                            sb.append(getValue(rightNode, right.get(Constants.FIRSTRESULT)));
                        }
                        break;
                    case Constants.ELEMENTOF:
                        if(!left.isEmpty() && left.size() == 1 && !right.isEmpty()){
                            sb.append(getValue(leftNode, left.get(Constants.FIRSTRESULT)));
                            sb.append(" ").append(op.getSymbol()).append(" ");
                            sb.append(joinOperandResults(rightNode, right));
                        }
                        break;
                    default:
                        return "Erro a construir expressão de uma expressão filha de um where. Operador desconhecido";
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão de uma expressão filha de um where. | ", e);
        }
        
        return sb.toString();
    }
    
    public String whereOperationBuilder(ValNode operandNode, ValResult operand, String expression){
        StringBuilder sb = new StringBuilder();
        
        try {
            String operandString = getValue(operandNode, operand);
            sb.append(operandString).append(" where (").append(expression).append(")");
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão do operador \"Where\". | ", e);
        }
        
        return sb.toString();
    }
    
    public String ifThenElseOperationBuilder(ValNode ifNode, ValNode thenNode, ValNode elseNode, ValResult ifResult, ValResult thenResult, ValResult elseResult){
        StringBuilder sb = new StringBuilder();
        
        try {
            String ifString = getValue(ifNode, ifResult);
            String thenString = getValue(thenNode, thenResult);
            String elseString = getValue(elseNode, elseResult);
            
            sb.append("if(").append(ifString).append(") ");
            sb.append("then(").append(thenString).append(") ");
            if(!elseString.isEmpty()) sb.append("else(").append(elseString).append(") ");
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão do operador \"If Then Else\". | ", e);
        }
        
        return sb.toString();
    }
    
    public String groupByOperationBuilder(List<ValNode> components){
        StringBuilder sb = new StringBuilder();

        try {
            List<ValResult> componentsResults = components.stream().map(ValNode::getResults).map(results -> results.get(Constants.FIRSTRESULT)).collect(Collectors.toList());

            String componentsString = String.join(", ", componentsResults.stream().map(ValResult::getRawValue).collect(Collectors.toList()));
            sb.append("group by (").append(componentsString).append(")");

        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão do operador \"Group By\". | ", e);
        }

        return sb.toString();
    }
    
    
    public String elementOfOperationBuilder(String value, List<String> setResults){
        StringBuilder sb = new StringBuilder();
        
        try {
            String setString = String.join(", ", setResults);
            sb.append(value).append(" in (").append(setString).append(")");
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão do operador \"Element Of\". | ", e);
        }
        
        return sb.toString();
    }
    
    public String filterOperationBuilder(ValNode left, ValNode right, ValResult leftResult, ValResult rightResult){
        StringBuilder sb = new StringBuilder();
        
        try {
            String leftString = getValue(left, leftResult);
            String rightString = getValue(right, rightResult);

            sb.append(leftString).append(" filter ").append(rightString);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão de \"Filter\". | ", e);
        }
        
        return sb.toString();
    }
    
    public String timeShiftOperationBuilder(String valueOfDimension, String period, String number, String dimension){
        StringBuilder sb = new StringBuilder();
        
        try {
            sb.append("time_shift(").append(valueOfDimension).append(", ");
            sb.append(period).append(", ");
            sb.append(number).append(", ");
            if(dimension != null && !dimension.isEmpty()) sb.append(dimension).append(")");
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão de \"TimeShift\". | ", e);
        }
        
        return sb.toString();
    }
    
    
    
    private String joinOperandResultsByPairs(List<Map.Entry<ValNode, ValResult>> nodeWithResults) {
        return nodeWithResults.stream()
                             .map(result -> getValue(result.getKey(), result.getValue()))
                             .collect(Collectors.joining(", "));
    }
    
    private String joinOperandResults(ValNode node, List<ValResult> nodeResults) {
        return nodeResults.stream()
                             .map(result -> getValue(node, result))
                             .collect(Collectors.joining(", "));
    }
    
    private String getValue(ValNode node, ValResult result) {
        if (nodeIsLeaf(node)) {
            return result.getRawValue();
        } else if (result != null && result.getExpression() != null && !result.getExpression().isEmpty()){
            return "(" + result.getExpression() + ")";
        } else {
            return "";
        }
    }

    private boolean nodeIsLeaf(ValNode node) {
        return OperationsUtils.nodeIsNotNull(node) && node.getNode().isLeaf();
    }

    
}
