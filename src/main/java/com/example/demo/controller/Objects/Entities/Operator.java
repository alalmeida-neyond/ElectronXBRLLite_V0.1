package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "OPERATOR")
@NamedQuery(name="Operator.findAll", query="SELECT o FROM Operator o")
public class Operator implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "OPERATORID")
    private int operatorID;
    
    @Column(name = "\"NAME\"")
    @NotNull
    @Size(max = 50)
    private String name;
    
    @Column(name = "SYMBOL")
    @NotNull
    @Size(max = 20)
    private String symbol;
    
    @Column(name = "TYPE")
    @NotNull
    @Size(max = 20)
    private String type;

    public int getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(int operatorID) {
        this.operatorID = operatorID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
