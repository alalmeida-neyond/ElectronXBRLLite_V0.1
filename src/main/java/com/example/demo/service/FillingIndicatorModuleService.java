package com.example.demo.service;

import java.util.*;

import com.example.demo.controller.Objects.Entities.*;

public class FillingIndicatorModuleService {
   
    //Gets a List of tableversions, get the corresponding TableVersion, based on the fillingindicaor (M_01.00/M_02.00)
    public static TableVersionDPM getTableVersionFromList(List<TableVersionDPM> listTableVersion,String fillingIndicatorCode){
        Optional<TableVersionDPM> possibleResult = listTableVersion.stream()
            .filter(tableversion -> tableversion.getCode().equals(fillingIndicatorCode))
            .findFirst();
        TableVersionDPM result =possibleResult.orElse(null);
        //listTableVersion.remove(result);
        return result;
    }
}
