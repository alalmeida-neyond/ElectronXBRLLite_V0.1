package com.example.demo.controller.Objects.Entities.DAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.DTOs.*;
import com.example.demo.Data.Access.*;
import com.example.demo.Resources.Constants;
import com.example.demo.Resources.Utils;
import com.example.demo.controller.Objects.Entities.Conf.ConfEntities;
import com.example.demo.controller.Objects.Entities.DPMOrigin.ModuleVersion;
import com.example.demo.controller.Objects.Import.*;


public class InImportedValuesDAL {

    public static List<ImportedValuesDTO> getListOfImportedValues(ModuleVersion module, LocalDate referenceDate, ConfEntities entity, String domain) {
        JPA<ImportedValuesDTO> jpa = new JPA<>(ImportedValuesDTO.class);
        domain = domain != null ? (domain.length() > Constants.DOMAINLENGTH ? domain.substring(0, 3) : domain) : null;
        List<ImportedValuesDTO> listOfValues = new ArrayList<>();
        try {
            listOfValues = jpa.getNativeResultListWithMapping(Utils.getResource("SQL_Queries/GetImportedValues.sql"), "ImportedValuesRow",
                    "actionId", String.valueOf(Constants.actionImport),
                    "typeStateOk", String.valueOf(Constants.tipoStateOK),
                    "desagregationCodeType", String.valueOf(Constants.DESAGREGATIONCODETYPE),
                    "desagregationCodeFixedType", String.valueOf(Constants.DESAGREGATIONCODETYPE),
                    "referenceDate", referenceDate != null ? referenceDate.format(Constants.dateFormat) : null,
                    "format",Constants.ISOBASEFORMAT,
                    "domain", domain != null ? domain.toUpperCase() : null,
                    "moduleVID", module != null ? String.valueOf(module.getModuleVID()) : null,
                    "entityId", entity != null ? String.valueOf(entity.getEntityID()) : null);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return listOfValues;
    }

    public static List<InImportedValuesTemp> getListOfImportedCellBasedOnImportedTable(InImportedTablesTemp impTab) {
        JPA<InImportedValuesTemp> jpa = new JPA<>(InImportedValuesTemp.class);
        List<InImportedValuesTemp> resultList = new ArrayList<>();
        try {
            resultList = jpa.getTypedNativeResultList("Select * from in_importedvaluestemp where importedTableId = ?importedTableId",
                    "importedTableId", impTab.getImportedTableId());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jpa.close();
        }
        return resultList;
    }
}
