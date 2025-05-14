/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.controller.Objects.Entities.*;


public class ValNode {
    
    private OperationNode node;
    private int level;
    private List<ValResult> results;
    
    public ValNode(){}
    
    public ValNode(OperationNode node, Integer level) {
        this.node = node;
        this.level = level;
    }
    
    public void setOnlyOneResult(ValResult oneResult){
        List<ValResult> parentResult = new ArrayList<>();
        parentResult.add(oneResult);
        this.results = parentResult;
    }
    
    public boolean hasOnlyOneResult(){
        if(this.results != null){
            return this.results.size() == 1;
        }
        return false;
    }
    
    public Operator getParentOperator(){
        if(node.getParentNode() == null) 
            return null;
        else 
            return node.getParentNode().getOperator();
    }
    
    public String getOperatorSymbol(){
        if(node.getOperator() != null) 
            return node.getOperator().getSymbol();
        else 
            return null;
    }
    
    public Integer getPreconditonOperationVId(){
        return node.getOperationVersion() != null && node.getOperationVersion().getPreConditionOperationVersion() != null 
                ? node.getOperationVersion().getPreConditionOperationVersion().getOperationVID() 
                : null;
    }
    
    public OperationVersion getOperationVersion(){
        return node.getOperationVersion() != null && node.getOperationVersion().getPreConditionOperationVersion() != null 
                    ? node.getOperationVersion()
                    : null;
    }

    public List<ValResult> getResults() {
        return results;
    }

    public void setResults(List<ValResult> results) {
        this.results = results;
    }

    public OperationNode getNode() {
        return node;
    }

    public void setNode(OperationNode node) {
        this.node = node;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    
    
    
}
