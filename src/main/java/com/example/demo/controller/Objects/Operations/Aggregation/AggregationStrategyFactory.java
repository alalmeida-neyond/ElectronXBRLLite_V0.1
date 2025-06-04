package com.example.demo.controller.Objects.Operations.Aggregation;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.Resources.Constants;

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
