package com.example.demo.controller.Objects.Comparison;

import java.math.BigDecimal;

import com.example.demo.controller.Objects.Validation.ValResult;

public interface ComparisonStrategy {
    ValResult compare(Object left, Object right, boolean useIntervals, BigDecimal leftMargin, BigDecimal rightMargin);
}
