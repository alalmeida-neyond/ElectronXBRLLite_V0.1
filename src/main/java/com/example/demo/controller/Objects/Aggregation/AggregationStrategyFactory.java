/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Aggregation;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.controller.Objects.Constants;

/**
 *
 * @author njesus
 */
public class AggregationStrategyFactory {
    
    private static final Map<Integer, AggregationStrategy> strategies = new HashMap<>();

    static {
        //Boolean
        strategies.put(Constants.SUM, new AggregationSumStrategy());
        strategies.put(Constants.COUNT, new AggregationCountStrategy());
        strategies.put(Constants.AGGREGATEMAXIMUM, new AggregationMaxStrategy());
        strategies.put(Constants.AGGREGATEMINIMUM, new AggregationMinStrategy());
    }

    public static AggregationStrategy getStrategy(int operatorId) {
        return strategies.get(operatorId);
    }
    
}
