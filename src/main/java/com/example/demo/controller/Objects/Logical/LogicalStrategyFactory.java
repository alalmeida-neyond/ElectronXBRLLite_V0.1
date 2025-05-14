/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Logical;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.controller.Objects.Constants;

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
