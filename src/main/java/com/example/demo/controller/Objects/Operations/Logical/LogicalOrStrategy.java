
package com.example.demo.controller.Objects.Operations.Logical;

public class LogicalOrStrategy implements LogicalStrategy{

    @Override
    public Boolean compare(Boolean left, Boolean right) {
        if (Boolean.TRUE.equals(left) || Boolean.TRUE.equals(right)) {
            return true;
        } else if (Boolean.FALSE.equals(left) && Boolean.FALSE.equals(right)) {
            return false;
        } else {
            return null;
        }
    }
    
}
