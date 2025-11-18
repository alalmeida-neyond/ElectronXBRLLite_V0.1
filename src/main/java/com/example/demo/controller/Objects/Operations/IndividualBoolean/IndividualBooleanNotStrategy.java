
package com.example.demo.controller.Objects.Operations.IndividualBoolean;

public class IndividualBooleanNotStrategy implements IndividualBooleanStrategy{

    @Override
    public Boolean evaluate(Object value) {
        if(value == null) return null;
        return !Boolean.valueOf(value.toString());
    }
}