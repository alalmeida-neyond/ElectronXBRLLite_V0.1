package com.example.demo.controller.Objects.Aggregation;

import java.util.List;

import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;

public interface AggregationStrategy {
    
    public ValResult evaluate(ValNode operandChild, List<ValResult> resultsGrouped);
}
