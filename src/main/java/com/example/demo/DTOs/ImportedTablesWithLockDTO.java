package com.example.demo.DTOs;

import com.example.demo.controller.Objects.Import.InImportedTablesTemp;

public class ImportedTablesWithLockDTO {
    private InImportedTablesTemp importedTable;
    private Boolean locked;

    public ImportedTablesWithLockDTO(InImportedTablesTemp importedTable, Boolean locked) {
        this.importedTable = importedTable;
        this.locked = locked;
    }

    public InImportedTablesTemp getImportedTable() {
        return importedTable;
    }

    public void setImportedTable(InImportedTablesTemp importedTable) {
        this.importedTable = importedTable;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
    
    
    
}
