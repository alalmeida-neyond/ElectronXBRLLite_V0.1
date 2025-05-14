/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityResult;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "OPERATIONNODE")
@SqlResultSetMapping(
        name = "OperationNodeMapping",
        entities = {
            @EntityResult(entityClass = OperationNode.class)
        },
        columns = {
            @ColumnResult(name = "nodeLevel", type = Integer.class)
        }
)
public class OperationNode implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "NODEID")
    private int nodeID;
    
    @JoinColumn(name = "OPERATIONVID")
    @ManyToOne(fetch = FetchType.LAZY)
    private OperationVersion operationVersion;
    
    @JoinColumn(referencedColumnName = "NODEID", name = "PARENTNODEID")
    @ManyToOne(fetch = FetchType.LAZY)
    private OperationNode parentNode;
    
    @JoinColumn(name = "OPERATORID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Operator operator;
    
    @JoinColumn(name = "ARGUMENTID")
    @ManyToOne(fetch = FetchType.LAZY)
    private OperatorArgument argument;
    
    @Column(name = "ABSOLUTETOLERANCE")
    private double absoluteTolerance;
    
    @Column(name = "RELATIVETOLERANCE")
    private double relativeTolerance;
    
    @Column(name = "FALLBACKVALUE")
    @Size(max = 50)
    private String fallbackValue;
    
    @Column(name = "USEINTERVALARITHMETICS", columnDefinition = "CHAR(1)", nullable = false)
    private boolean useIntervalArithmetics;
    
    @Column(name = "OPERANDTYPE")
    @Size(max = 20)
    private String operandType;
    
    @Column(name = "ISLEAF", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isLeaf;
    
    @Column(name = "SCALAR")
    @Lob
    private String scalar;

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public OperationVersion getOperationVersion() {
        return operationVersion;
    }

    public void setOperationVersion(OperationVersion operationVersion) {
        this.operationVersion = operationVersion;
    }

    public OperationNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(OperationNode parentNode) {
        this.parentNode = parentNode;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public OperatorArgument getArgument() {
        return argument;
    }

    public void setArgument(OperatorArgument argument) {
        this.argument = argument;
    }

    public double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    public void setAbsoluteTolerance(double absoluteTolerance) {
        this.absoluteTolerance = absoluteTolerance;
    }

    public double getRelativeTolerance() {
        return relativeTolerance;
    }

    public void setRelativeTolerance(double relativeTolerance) {
        this.relativeTolerance = relativeTolerance;
    }

    public String getFallbackValue() {
        return fallbackValue;
    }

    public void setFallbackValue(String fallbackValue) {
        this.fallbackValue = fallbackValue;
    }

    public boolean isUseIntervalArithmetics() {
        return useIntervalArithmetics;
    }

    public void setUseIntervalArithmetics(boolean useIntervalArithmetics) {
        this.useIntervalArithmetics = useIntervalArithmetics;
    }

    public String getOperandType() {
        return operandType;
    }

    public void setOperandType(String operandType) {
        this.operandType = operandType;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public String getScalar() {
        return scalar;
    }

    public void setScalar(String scalar) {
        this.scalar = scalar;
    }    
}
