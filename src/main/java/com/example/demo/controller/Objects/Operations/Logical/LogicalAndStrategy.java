
package com.example.demo.controller.Objects.Operations.Logical;

public class LogicalAndStrategy implements LogicalStrategy{

    @Override
    public Boolean compare(Boolean left, Boolean right) {
        if (left == null || right == null) {
            if (left == null && right == null) {
                return null;
            } else if (Boolean.FALSE.equals(left) || Boolean.FALSE.equals(right)) {
                return false;
            } else {
                return null;
            }
        } else {
            return left && right;
        }
    }
    
}
