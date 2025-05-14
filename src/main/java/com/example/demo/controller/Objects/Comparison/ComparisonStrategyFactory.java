/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Comparison;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.controller.Objects.Constants;

import java.util.AbstractMap;

/**
 *
 * @author njesus
 */
public class ComparisonStrategyFactory {

    private static final Map<Map.Entry<Integer, Integer>, ComparisonStrategy> strategies = new HashMap<>();

    static {
        //Decimal
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DECIMAL, Constants.EQUALSTO), new DecimalEqualsStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DECIMAL, Constants.LESSTHANEQUALTO), new DecimalLessThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DECIMAL, Constants.GREATERTHANEQUALTO), new DecimalGreaterThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DECIMAL, Constants.GREATERTHAN), new DecimalGreaterThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DECIMAL, Constants.LESSTHAN), new DecimalLessThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DECIMAL, Constants.NOTEQUALTO), new DecimalNotEqualsStrategy());
        //String
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.STRINGINCLUDINGEMPTY, Constants.EQUALSTO), new StringEqualsStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.STRINGINCLUDINGEMPTY, Constants.LESSTHANEQUALTO), new StringLessThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.STRINGINCLUDINGEMPTY, Constants.GREATERTHANEQUALTO), new StringGreaterThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.STRINGINCLUDINGEMPTY, Constants.GREATERTHAN), new StringGreaterThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.STRINGINCLUDINGEMPTY, Constants.LESSTHAN), new StringLessThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.STRINGINCLUDINGEMPTY, Constants.NOTEQUALTO), new StringNotEqualsStrategy());
        //Booleanos
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.BOOLEAN, Constants.EQUALSTO), new BooleanEqualsStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.BOOLEAN, Constants.LESSTHANEQUALTO), new BooleanLessThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.BOOLEAN, Constants.GREATERTHANEQUALTO), new BooleanGreaterThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.BOOLEAN, Constants.GREATERTHAN), new BooleanGreaterThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.BOOLEAN, Constants.LESSTHAN), new BooleanLessThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.BOOLEAN, Constants.NOTEQUALTO), new BooleanNotEqualsStrategy());
        //DateTime
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATETIME, Constants.EQUALSTO), new DateTimeEqualsStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATETIME, Constants.LESSTHANEQUALTO), new DateTimeLessThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATETIME, Constants.GREATERTHANEQUALTO), new DateTimeGreaterThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATETIME, Constants.GREATERTHAN), new DateTimeGreaterThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATETIME, Constants.LESSTHAN), new DateTimeLessThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATETIME, Constants.NOTEQUALTO), new DateTimeNotEqualsStrategy());
    }

    public static ComparisonStrategy getStrategy(int dataTypeId, int operatorId) {
        return strategies.get(new AbstractMap.SimpleEntry<>(dataTypeId, operatorId));
    }
}
