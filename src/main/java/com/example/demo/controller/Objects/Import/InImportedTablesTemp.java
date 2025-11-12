package com.example.demo.controller.Objects.Import;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.demo.Converter.LocalDateTimePersistenceConverter;
import com.example.demo.DTOs.CommonDatapointValidationDTO;
import com.example.demo.DTOs.ImportedFilesResumeDTO;
import com.example.demo.controller.Objects.Entities.DPMOrigin.TableVersionDPM;
import com.example.demo.controller.Objects.Entities.DPMOrigin.VariableVersion;
import com.example.demo.controller.Objects.IO.IO;
import com.example.demo.controller.Objects.IO.IOState;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;


@Entity
@Table(name = "IN_IMPORTEDTABLESTEMP", schema = "DPM_ED")
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "TablesWithLock",
            entities = {
                @EntityResult(entityClass = InImportedTablesTemp.class),},
            columns = {
                @ColumnResult(name = "locked", type = Boolean.class),}
    ),
    @SqlResultSetMapping(
            name="CommonDatapointValidation",
            classes = {
                @ConstructorResult(
                        targetClass = CommonDatapointValidationDTO.class,
                        columns = {
                            @ColumnResult(name = "details", type = String.class),
                            @ColumnResult(name = "domain", type = String.class)
                        }
                )
            }
    ),
    @SqlResultSetMapping(
            name = "ImportedFilesResumeRow",
            classes = {
                @ConstructorResult(
                        targetClass = ImportedFilesResumeDTO.class,
                        columns = {
                            @ColumnResult(name = "referenceDate", type = String.class),
                            @ColumnResult(name = "entity", type = String.class),
                            @ColumnResult(name = "domain", type = String.class),
                            @ColumnResult(name = "module", type = String.class),
                            @ColumnResult(name = "tableCode", type = String.class),
                            @ColumnResult(name = "desagCode", type = String.class),
                            @ColumnResult(name = "isMandatory", type = Integer.class),
                            @ColumnResult(name = "isImported", type = Integer.class),
                            @ColumnResult(name = "importTimestamp", type = String.class),
                            @ColumnResult(name = "userID", type = String.class)
                        }
                )
            })
})
public class InImportedTablesTemp implements Serializable {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "impTableSequenceTemp_gen")
    @SequenceGenerator(name = "impTableSequenceTemp_gen", sequenceName = "DPM_ED.IMPTABLESTEMP_SEQ", allocationSize = 1)
    @Column(name = "IMPORTEDTABLEID")
    private int importedTableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IOID", referencedColumnName = "IOID", nullable = false)
    private IO io;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TABLEVID", referencedColumnName = "TABLEVID", nullable = false)
    private TableVersionDPM tableVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VARIABLEVID", referencedColumnName = "VARIABLEVID")
    private VariableVersion variableVersion;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinColumn(name = "IMPORTKEYID", referencedColumnName = "IMPORTKEYID")
    private InImportKey importKey;

    @Column(name = "INITTIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime initTimestamp;

    @Column(name = "ENDTIMESTAMP")
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime endTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IO_STATEID", referencedColumnName = "IO_STATEID", nullable = false)
    private IOState ioState;

    @Column(name = "DESAGREGATIONCODE")
    private String desagregationCode;

    public InImportedTablesTemp() {
    }

    public InImportedTablesTemp(IO io, TableVersionDPM tableVersion, VariableVersion variableVersion, InImportKey importKey, LocalDateTime initTimestamp, LocalDateTime endTimestamp, IOState ioState, String desagregationCode) {
        this.io = io;
        this.tableVersion = tableVersion;
        this.variableVersion = variableVersion;
        this.importKey = importKey;
        this.initTimestamp = initTimestamp;
        this.endTimestamp = endTimestamp;
        this.ioState = ioState;
        this.desagregationCode = desagregationCode;
    }
    
    public boolean isOpenRows(){
        return this.getTableVersion().isOpenRows();
    }
    
    public String getCodeOfMap(){
        return this.tableVersion.getCode();
    }
    
    public int getTableVID(){
        return this.tableVersion != null ? this.tableVersion.getTableVID() : null;
    }

    public int getImportedTableId() {
        return importedTableId;
    }

    public void setImportedTableId(int importedTableId) {
        this.importedTableId = importedTableId;
    }

    public IO getIo() {
        return io;
    }

    public void setIo(IO io) {
        this.io = io;
    }

    public TableVersionDPM getTableVersion() {
        return tableVersion;
    }

    public void setTableVersion(TableVersionDPM tableVersion) {
        this.tableVersion = tableVersion;
    }

    public VariableVersion getVariableVersion() {
        return variableVersion;
    }

    public void setVariableVersion(VariableVersion variableVersion) {
        this.variableVersion = variableVersion;
    }

    public InImportKey getImportKey() {
        return importKey;
    }

    public void setImportKey(InImportKey importKey) {
        this.importKey = importKey;
    }

    public LocalDateTime getInitTimestamp() {
        return initTimestamp;
    }

    public void setInitTimestamp(LocalDateTime initTimestamp) {
        this.initTimestamp = initTimestamp;
    }

    public LocalDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(LocalDateTime endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public IOState getIoState() {
        return ioState;
    }

    public void setIoState(IOState ioState) {
        this.ioState = ioState;
    }

    public String getDesagregationCode() {
        return desagregationCode;
    }

    public void setDesagregationCode(String desagregationCode) {
        this.desagregationCode = desagregationCode;
    }
}