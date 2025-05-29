package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import com.example.demo.Data.Access.JPA;
import com.example.demo.controller.Objects.*;
public class ValidationService {

    public List<Object[]> getValidationResults() {
        JPA<Object[]> jpa = new JPA<>(Object[].class);
        List<Object[]> results = new ArrayList<>();
        try {
            results = jpa.getMappedFileQueryResultList(Utils.getResource("GetValidationsResultsDetails.sql"),
                    "ValidationResultsDetailsRow");

            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
