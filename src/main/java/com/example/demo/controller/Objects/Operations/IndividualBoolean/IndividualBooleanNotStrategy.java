/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.Operations.IndividualBoolean;

public class IndividualBooleanNotStrategy implements IndividualBooleanStrategy{

    @Override
    public Boolean evaluate(Object value) {
        if(value == null) return null;
        return !Boolean.valueOf(value.toString());
    }
}