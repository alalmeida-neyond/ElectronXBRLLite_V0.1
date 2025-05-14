/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects.IndividualBoolean;

public class IndividualBooleanIsNullStrategy implements IndividualBooleanStrategy{

    @Override
    public Boolean evaluate(Object value) {
        return value == null;
    }
    
    
    
}
