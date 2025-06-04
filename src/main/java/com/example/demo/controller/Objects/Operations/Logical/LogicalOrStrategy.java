/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
