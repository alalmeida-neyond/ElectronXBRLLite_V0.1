/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.Map;
import java.util.AbstractMap;
import java.util.logging.Logger;

public class OperationDomainBuilder {

    private final static Logger LOG = Logger.getLogger(OperationDomainBuilder.class.getName());

    public List<String> binaryOperationDomainBuilder(ValResult leftResult, ValResult rightResult) {
        List<String> domain = new ArrayList();

        try {
            List<String> leftDomain = getDomain(leftResult);
            List<String> rightDomain = getDomain(rightResult);

            domain.addAll(leftDomain);
            domain.addAll(rightDomain);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir domínio de uma expressão binária. | ", e);
        }

        return domain;
    }
    
    public List<String> individualResultDomainBuilder(ValResult operandResult){
        List<String> domain = new ArrayList();

        try {
            List<String> operandDomain = getDomain(operandResult);
            domain.addAll(operandDomain);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir domínio de uma valor individual. | ", e);
        }
        
        return domain;
    }
    
    public List<String> groupOfResultsDomainBuilder(List<ValResult> operandResults){
        List<String> domain = new ArrayList();

        try {
            for(ValResult operand : operandResults){
                List<String> operandDomain = getDomain(operand);
                domain.addAll(operandDomain);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir domínio de uma expressão de agregacao. | ", e);
        }
        
        return domain;
    }
    
    public List<String> aggregateNumericDomainBuilder(List<Map.Entry<ValNode, ValResult>> operands){
        List<String> domain = new ArrayList();
        
        try {
            for(Map.Entry<ValNode, ValResult> operand : operands){
                List<String> operandDomain = getDomain(operand.getValue());
                domain.addAll(operandDomain);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir domínio de uma expressão de agregacao numérica. | ", e);
        }
        
        return domain;
    }
    
    /*public List<String> parenthesisOperatonBuilder(ValResult operandResult){
        List<String> domain = new ArrayList();
        
        try {
            List<String> operandDomain = getDomain(operandResult);
            domain.addAll(operandDomain);
        } catch (Exception e) {
            LOG.error("Erro a construir domínio do operador \"Parenthesis\". | ", e);
        }
        
        return domain;
    }*/
    
    public List<String> childWhereDomainBuilder(ValNode leftNode, ValNode rightNode){
        List<String> domain = new ArrayList();

        List<ValResult> left = new ArrayList<>();
        List<ValResult> right = new ArrayList<>();
        
        try {
            if(OperationsUtils.nodeIsNotNull(leftNode) && OperationsUtils.resultsIsNotEmpty(leftNode)) left = leftNode.getResults();
            if(OperationsUtils.nodeIsNotNull(rightNode) && OperationsUtils.resultsIsNotEmpty(rightNode)) right = rightNode.getResults();
            
            domain.addAll(groupOfResultsDomainBuilder(left));
            domain.addAll(groupOfResultsDomainBuilder(right));
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão de uma expressão filha de um where. | ", e);
        }
        
        return domain;
    }
    
    public List<String> ifThenElseDomainBuilder(ValResult ifResult, ValResult thenResult, ValResult elseResult){
        List<String> domain = new ArrayList<>();
        
        try {
            List<String> ifDomain = getDomain(ifResult);
            List<String> thenDomain = getDomain(thenResult);
            List<String> elseDomain = getDomain(elseResult);
            
            domain.addAll(ifDomain);
            domain.addAll(thenDomain);
            domain.addAll(elseDomain);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Erro a construir expressão do operador \"If Then Else\". | ", e);
        }
        
        return domain;
    }

    
    private List<String> getDomain(ValResult result) {
        if (result != null && result.getDomain() != null && !result.getDomain().isEmpty()) {
            return result.getDomain();
        } else {
            return new ArrayList<>();
        }
    }

}
