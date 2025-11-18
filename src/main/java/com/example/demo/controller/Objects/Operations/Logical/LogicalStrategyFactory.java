
package com.example.demo.controller.Objects.Operations.Logical;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.Resources.Constants;

/**
 *
 * @author njesus
 */
public class LogicalStrategyFactory {
    private static final Map<Integer, LogicalStrategy> strategies = new HashMap<>();

    static {
        strategies.put(Constants.AND, new LogicalAndStrategy());
        strategies.put(Constants.OR, new LogicalOrStrategy());
        strategies.put(Constants.EXCLUSIVEOR, new LogicalExclusiveOrStrategy());
    }

    public static LogicalStrategy getStrategy(int operatorId) {
        return strategies.get(operatorId);
    }
}
