/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author njesus
 */
public class ValResult {
    
    private ValKey key;
    private ValValue result;
    private String refDate;
    private String expression;
    private BigDecimal margin;
    private boolean usedMargin;
    private BigDecimal difference;
    private List<String> domain;
    
    public ValResult(){}

    public ValResult(ValKey key, ValValue result, String refDate, BigDecimal margin, List<String> domain) {
        this.key = key;
        this.result = result;
        this.margin = margin;
        
        this.domain = new ArrayList<>();
        if(domain != null){
            this.domain.addAll(domain);
        }
    }
    
    public ValResult(ValKey key, ValValue result, BigDecimal margin) {
        this.key = key;
        this.result = result;
        this.margin = margin;
    }
    
    public ValResult(ValKey key, ValValue result){
        this.key = key;
        this.result = result;
        this.margin = BigDecimal.ZERO;
    }
    
    public ValResult(ValKey key){
        this.key = key;
    }
    
    public ValResult(ValValue result){
        this.result = result;
    }
    
    public ValResult(ValValue result, BigDecimal margin) {
        this.result = result;
        this.margin = margin;
    }
    
    public ValResult(ValKey key, ValValue result, String refDate, String domain){
        this.key = key;
        this.result = result;
        this.refDate = refDate;
        
        this.domain = new ArrayList<>();
        if(domain != null){
            this.domain.add(domain);
        }
    }
    
    public String getRawValue(){
        if(this.result != null){
            return this.result.getValue();
        }
        return null;
    }
    
    public boolean valueIsNull(){
        if(this.result == null) return true;
        else return this.result.getValue() == null;
    }
    
    public boolean isItem(){
        return refDate == null && result.getDatatype().getDataTypeId() == Constants.ENUMERATION;
    }
    
    public ValKey getKey() {
        return key;
    }

    public void setKey(ValKey key) {
        this.key = key;
    }

    public ValValue getResult() {
        return result;
    }

    public void setResult(ValValue result) {
        this.result = result;
    }

    public String getRefDate() {
        return refDate;
    }

    public void setRefDate(String refDate) {
        this.refDate = refDate;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    } 

    public boolean isUsedMargin() {
        return usedMargin;
    }

    public void setUsedMargin(boolean usedMargin) {
        this.usedMargin = usedMargin;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }

    public List<String> getDomain() {
        return domain;
    }

    public void setDomain(List<String> domain) {
        this.domain = domain;
    }
    
    @Override
    public String toString() {
        String keyString = (key != null) ? "Chave: " + key.toString() : "Chave: null";
        return "Resultado: "+getRawValue()+" | " + keyString +"\n";
    }
    
    
}
