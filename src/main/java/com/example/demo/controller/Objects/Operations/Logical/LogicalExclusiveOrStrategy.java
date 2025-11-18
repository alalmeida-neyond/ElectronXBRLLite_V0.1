
package com.example.demo.controller.Objects.Operations.Logical;

public class LogicalExclusiveOrStrategy implements LogicalStrategy{

    @Override
    public Boolean compare(Boolean left, Boolean right) {
        if (left == null || right == null) {
            return null;
        } else {
            return left ^ right;
        }
    }
    
}