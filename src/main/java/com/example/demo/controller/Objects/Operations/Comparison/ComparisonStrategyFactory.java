/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Operations.Comparison;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.Resources.Constants;

import java.util.AbstractMap;
import java.util.ArrayList;

/**
 *
 * @author njesus
 */
public class ComparisonStrategyFactory {

    private static final Map<Map.Entry<Integer, Integer>, ComparisonStrategy> strategies = new HashMap<>();
    private static final ArrayList<Integer> numericDataTypeIds = new ArrayList<>();

    static {
        //Decimal
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDECIMAL, Constants.EQUALSTO), new DecimalEqualsStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDECIMAL, Constants.LESSTHANEQUALTO), new DecimalLessThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDECIMAL, Constants.GREATERTHANEQUALTO), new DecimalGreaterThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDECIMAL, Constants.GREATERTHAN), new DecimalGreaterThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDECIMAL, Constants.LESSTHAN), new DecimalLessThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDECIMAL, Constants.NOTEQUALTO), new DecimalNotEqualsStrategy());
        //String
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPESTRINGINCLUDINGEMPTY, Constants.EQUALSTO), new StringEqualsStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPESTRINGINCLUDINGEMPTY, Constants.LESSTHANEQUALTO), new StringLessThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPESTRINGINCLUDINGEMPTY, Constants.GREATERTHANEQUALTO), new StringGreaterThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPESTRINGINCLUDINGEMPTY, Constants.GREATERTHAN), new StringGreaterThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPESTRINGINCLUDINGEMPTY, Constants.LESSTHAN), new StringLessThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPESTRINGINCLUDINGEMPTY, Constants.NOTEQUALTO), new StringNotEqualsStrategy());
        //Booleanos
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEBOOLEAN, Constants.EQUALSTO), new BooleanEqualsStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEBOOLEAN, Constants.LESSTHANEQUALTO), new BooleanLessThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEBOOLEAN, Constants.GREATERTHANEQUALTO), new BooleanGreaterThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEBOOLEAN, Constants.GREATERTHAN), new BooleanGreaterThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEBOOLEAN, Constants.LESSTHAN), new BooleanLessThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEBOOLEAN, Constants.NOTEQUALTO), new BooleanNotEqualsStrategy());
        //DateTime
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDATETIME, Constants.EQUALSTO), new DateTimeEqualsStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDATETIME, Constants.LESSTHANEQUALTO), new DateTimeLessThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDATETIME, Constants.GREATERTHANEQUALTO), new DateTimeGreaterThanEqualToStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDATETIME, Constants.GREATERTHAN), new DateTimeGreaterThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDATETIME, Constants.LESSTHAN), new DateTimeLessThanStrategy());
        strategies.put(new AbstractMap.SimpleEntry<>(Constants.DATATYPEDATETIME, Constants.NOTEQUALTO), new DateTimeNotEqualsStrategy());
    }
    
    static {
        numericDataTypeIds.add(Constants.DATATYPEDECIMAL);
        numericDataTypeIds.add(Constants.DATATYPEINTEGER);
        numericDataTypeIds.add(Constants.DATATYPEPERCENTAGE);
        numericDataTypeIds.add(Constants.DATATYPEMONETARY);
    }

    public static ComparisonStrategy getStrategy(int dataTypeId, int operatorId) {
        if(numericDataTypeIds.contains(dataTypeId)){
            dataTypeId = Constants.DATATYPEDECIMAL;
        }
        return strategies.get(new AbstractMap.SimpleEntry<>(dataTypeId, operatorId));
    }
}
