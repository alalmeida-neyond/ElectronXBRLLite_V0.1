package com.example.demo.controller.Objects.Operations.NumericAggregationStrategy;

import java.util.List;
import java.util.Map;

import com.example.demo.controller.Objects.Validation.ValNode;
import com.example.demo.controller.Objects.Validation.ValResult;

public interface NumericAggregationStrategy {
    
    public ValResult evaluate(ValNode parent, List<Map.Entry<ValNode, ValResult>> resultsGrouped);
    
}
