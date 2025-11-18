
package com.example.demo.controller.Objects.Operations.IndividualBoolean;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.Resources.Constants;

public class IndividualBooleanStrategyFactory {
    
    private static final Map<Integer, IndividualBooleanStrategy> strategies = new HashMap<>();

    static {
        //Decimal
        strategies.put(Constants.NOT, new IndividualBooleanNotStrategy());
        strategies.put(Constants.ISNULL, new IndividualBooleanIsNullStrategy());
    }

    public static IndividualBooleanStrategy getStrategy(int operatorId) {
        return strategies.get(operatorId);
    }
    
    
}
