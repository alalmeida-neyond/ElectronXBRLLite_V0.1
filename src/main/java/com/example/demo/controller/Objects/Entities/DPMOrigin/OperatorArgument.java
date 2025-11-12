package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = "OPERATORARGUMENT", schema = "DPM_MD")
@NamedQuery(name="OperatorArgument.findAll", query="SELECT o FROM OperatorArgument o")
public class OperatorArgument implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "ARGUMENTID")
    private int argumentID;
    
    @JoinColumn(name = "OPERATORID")
    @ManyToOne
    private Operator operator;
    
    @Column(name = "\"Order\"")
    @NotNull
    private int order;
    
    @Column(name = "ISMANDATORY", columnDefinition = "CHAR(1)", nullable = false)
    private boolean isMandatory;
    
    @Column(name = "\"NAME\"")
    @NotNull
    @Size(max = 50)
    private String name;

    public int getArgumentID() {
        return argumentID;
    }

    public void setArgumentID(int argumentID) {
        this.argumentID = argumentID;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
