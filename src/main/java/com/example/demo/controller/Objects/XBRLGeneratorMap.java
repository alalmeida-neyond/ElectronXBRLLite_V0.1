/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controller.Objects;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.example.demo.controller.Objects.DAL.*;
import com.example.demo.controller.Objects.Import.*;


public class XBRLGeneratorMap implements Runnable {

    private String path;
    private List<InImportedTablesTemp> listOfImportedTables;
    private LocalDate referenceDate;
    private Boolean altXBRLGeneration;
    private final Logger LOG = Logger.getLogger(XBRLGeneratorMap.class);


    public XBRLGeneratorMap(String path, List<InImportedTablesTemp> listOfImportedTables, LocalDate referenceDate, Boolean altXBRLGeneration) {
        this.path = path;
        this.listOfImportedTables = listOfImportedTables;
        this.referenceDate = referenceDate;
        this.altXBRLGeneration = altXBRLGeneration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<InImportedTablesTemp> getListOfImportedTables() {
        return listOfImportedTables;
    }

    public void setListOfImportedTables(List<InImportedTablesTemp> listOfImportedTables) {
        this.listOfImportedTables = listOfImportedTables;
    }

    @Override
    public void run() {
        populateCSV();
    }

    public void populateCSV() {
        if (listOfImportedTables.isEmpty()) {
            return;
        }
        List<Object[]> header;
        if(altXBRLGeneration){
            header = TableVersionHeaderDAL.getAltGenerationHeader(listOfImportedTables.get(0).getTableVersion().getTableVID(), referenceDate);
        }else{
            header = TableVersionHeaderDAL.getGenerationHeader(listOfImportedTables.get(0).getTableVersion().getTableVID(), referenceDate);
        }
        Path fillingCSVPath = Paths.get(path + Utils.getSeparator(), listOfImportedTables.get(0).getTableVersion().getCode().toLowerCase() + ".csv");
        Map<Integer, List<InKeyAssociation>> keyAssociationsFromTables = new HashMap<Integer, List<InKeyAssociation>>();
        Map<Integer, List<InKeyAssociation>> keyAssociationsFromCells = new HashMap<Integer, List<InKeyAssociation>>();
        boolean firstLoopInHeaderForAlt = true;
        try (FileWriter writer = new FileWriter(fillingCSVPath.toFile())) {
            if(altXBRLGeneration){
                for (Object[] obj : header) {
                    if(!firstLoopInHeaderForAlt){
                        writer.append(",");
                    }else{
                        firstLoopInHeaderForAlt = false;
                    }
                    writer.append("c"+obj[1].toString());
                }
            }else{
                writer.append("datapoint,factValue");
                for (Object[] obj : header) {
                    writer.append(",");
                    writer.append(obj[0].toString());
                }
            }

            List<InImportKey> importKeysFromTables = listOfImportedTables.stream().map(InImportedTablesTemp::getImportKey).distinct().collect(Collectors.toList());
            keyAssociationsFromTables = InKeyAssociationDAL.getListOfKeyAssociationsBasedOnImportKey(importKeysFromTables);

            for (InImportedTablesTemp impTab : listOfImportedTables) {
                List<InImportedValuesTemp> cells = InImportedValuesDAL.getListOfImportedCellBasedOnImportedTable(impTab);
                if(altXBRLGeneration){
                    Map<InImportKey,List<InImportedValuesTemp>> mappedByRowkey = cells.stream()
                                                                                .collect(Collectors.toMap(
                                                                                                    InImportedValuesTemp::getImportKey,
                                                                                                    x -> {
                                                                                                        List<InImportedValuesTemp> list = new ArrayList<>();
                                                                                                        list.add(x);
                                                                                                        return list;
                                                                                                    },
                                                                                                    (left, right) -> {
                                                                                                         left.addAll(right);
                                                                                                         return left;
                                                                                                    },
                                                                                                    HashMap::new

                                                                                             )
                                                                                );
                    for(Map.Entry<InImportKey,List<InImportedValuesTemp>> entry : mappedByRowkey.entrySet()){
                        List<InImportedValuesTemp> rowValues = entry.getValue();
                        firstLoopInHeaderForAlt = true;
                        writer.append("\n");
                        for (Object[] obj : header) {
                            if(!firstLoopInHeaderForAlt){
                                writer.append(",");
                            }else{
                                firstLoopInHeaderForAlt = false;
                            }
                            if(obj[2].toString().equals("1")){ //KEY
                                Optional<InKeyAssociation> resultKeyToWrite = entry.getKey().getListPropertyValues().stream()
                                                                                            .filter(rowkey -> rowkey.getPropertyName().equals(obj[0].toString()))
                                                                                            .findFirst();
                                writer.append(resultKeyToWrite.isPresent() ? resultKeyToWrite.get().getPropertyValue() : "");
                            }else{
                                Optional<InImportedValuesTemp> resultToWrite = rowValues.stream()
                                                        .filter(row -> row.getColuna().equals(obj[1].toString()))
                                                        .findFirst();
                                
                                writer.append(resultToWrite.isPresent() ? resultToWrite.get().getRuleValue() : "");
                            }
                        }
                    }
                }else{
                    List<InKeyAssociation> keysDesagregationCode = new ArrayList<InKeyAssociation>();
                    if (impTab.getImportKey() != null && keyAssociationsFromTables.containsKey(impTab.getImportKey().getImportKeyID())) {
                        keysDesagregationCode = keyAssociationsFromTables.get(impTab.getImportKey().getImportKeyID());
                    }
                    if (cells != null) {
                        List<InImportKey> importKeysFromCells = cells.stream().map(InImportedValuesTemp::getImportKey).distinct().collect(Collectors.toList());
                        keyAssociationsFromCells = InKeyAssociationDAL.getListOfKeyAssociationsBasedOnImportKey(importKeysFromCells);
                    }
                    for (InImportedValuesTemp cell : cells) {
                        List<InKeyAssociation> keysRowKey = new ArrayList<InKeyAssociation>();
                        if (cell.getImportKey() != null && keyAssociationsFromCells.containsKey(cell.getImportKey().getImportKeyID())) {
                            keysRowKey = keyAssociationsFromCells.get(cell.getImportKey().getImportKeyID());
                        }
                        writer.append("\n");
                        writer.append("dp");
                        writer.append(cell.getVariableVersion().getVariableVID() + ",");
                        writer.append(cell.getRuleValue());
                        if (!header.isEmpty()) {
                            for (Object[] obj : header) {
                                boolean found = false;
                                if (!keysRowKey.isEmpty()) {
                                    for (InKeyAssociation key : keysRowKey) {
                                        if (key.getPropertyName().toUpperCase().equals(obj[0].toString().toUpperCase())) {
                                            writer.append("," + key.getPropertyValue());
                                            found = true;
                                            break;
                                        }
                                    }
                                }

                                //IF found it isn't the desagregationCode
                                if (!found) {
                                    //desagregationCode 
                                    if (!keysDesagregationCode.isEmpty()) {
                                        if(keysDesagregationCode.get(0).getImportedKey().getKeyType().getKeyTypeID() == Constants.DESAGREGATIONCODEFIXEDTYPE){
                                            continue;
                                        }
                                        for (InKeyAssociation key : keysDesagregationCode) {
                                            if (key.getPropertyName().toUpperCase().equals(obj[0].toString().toUpperCase())) {
                                                writer.append("," + key.getPropertyValue());
                                                break;
                                            }
                                        }
                                    }
                                }

                                //Not filled values
                                if(!found){
                                    writer.append(",");
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Erro na populacao do CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    

}
