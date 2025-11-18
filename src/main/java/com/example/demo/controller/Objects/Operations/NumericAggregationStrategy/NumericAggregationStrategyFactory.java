
package com.example.demo.controller.Objects.Operations.NumericAggregationStrategy;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.Resources.Constants;

public class NumericAggregationStrategyFactory {
    
    private static final Map<Integer, NumericAggregationStrategy> strategies = new HashMap<>();

    static {
        //Boolean
        strategies.put(Constants.NUMERICMINIMUM, new NumericAggregationMinStrategy());
        strategies.put(Constants.NUMERICMAXIMUM, new NumericAggregationMaxStrategy());
    }

    public static NumericAggregationStrategy getStrategy(int operatorId) {
        return strategies.get(operatorId);
    }
}
