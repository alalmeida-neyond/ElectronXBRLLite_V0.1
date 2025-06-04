/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Operations.Where;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.Resources.Constants;

public class AggregatorStrategyFactory {
    
    private static final Map<Integer, WhereStrategy> strategies = new HashMap<>();

    static {
        //Boolean
        strategies.put(Constants.EQUALSTO, new WhereEqualsToStrategy());
        strategies.put(Constants.NOTEQUALTO, new WhereNotEqualsToStrategy());
        strategies.put(Constants.ELEMENTOF, new WhereElementOfStrategy());
    }

    public static WhereStrategy getStrategy(int operatorId) {
        return strategies.get(operatorId);
    }
    
}
