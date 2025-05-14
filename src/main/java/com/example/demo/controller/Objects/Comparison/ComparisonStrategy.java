/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.demo.controller.Objects.Comparison;

import java.math.BigDecimal;

import com.example.demo.controller.Objects.ValResult;

public interface ComparisonStrategy {
    ValResult compare(Object left, Object right, boolean useIntervals, BigDecimal leftMargin, BigDecimal rightMargin);
}
