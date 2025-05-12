package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.DTOs.HeaderDTO;

public class HeaderService {
    
    
    //Gets the Header and removes the one that already exists;
    public static HeaderDTO getHeaderDTOFromList(List<HeaderDTO> listHeaders, String code, char direction, boolean openRow){
        Optional<HeaderDTO> possibleResult = listHeaders.stream()
            .filter(header -> (header.getCode().equals(code) && header.getDirection() == direction))
            .findFirst();
        HeaderDTO result =possibleResult.orElse(null);
        if(!openRow){
            listHeaders.remove(result);
        }
        return result;
    }
    
}