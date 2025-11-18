
package com.example.demo.controller.Objects.Operations.IndividualBoolean;

public class IndividualBooleanIsNullStrategy implements IndividualBooleanStrategy{

    @Override
    public Boolean evaluate(Object value) {
        return value == null;
    }
    
    
    
}
