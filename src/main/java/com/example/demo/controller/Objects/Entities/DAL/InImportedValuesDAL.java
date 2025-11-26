package com.example.demo.controller.Objects.Entities.DAL;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.Data.Access.*;
import com.example.demo.controller.Objects.Import.*;


public class InImportedValuesDAL {

    public static List<InImportedValuesTemp> getListOfImportedCellBasedOnImportedTable(InImportedTablesTemp impTab) {
        JPA<InImportedValuesTemp> jpa = new JPA<>(InImportedValuesTemp.class);
        List<InImportedValuesTemp> resultList = new ArrayList<>();
        try {
            resultList = jpa.getTypedNativeResultList("Select * from DPM_OD.in_importedvaluestemp where importedTableId = ?importedTableId",
                    "importedTableId", impTab.getImportedTableId());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return resultList;
    }
}
