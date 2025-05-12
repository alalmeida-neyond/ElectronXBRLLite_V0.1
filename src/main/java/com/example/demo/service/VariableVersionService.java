package com.example.demo.service;

import java.util.List;

import com.example.demo.controller.Objects.Entities.*;

public class VariableVersionService {
    
    public static VariableVersion getVariableVersionFromCode(List<VariableVersion> variableVersionList, String code){
        VariableVersion result = null;
        for(VariableVersion varVersion : variableVersionList){
            if(varVersion.getCode().equals(code)){
                result = varVersion;
            }
        }
        return result;
    }
}
