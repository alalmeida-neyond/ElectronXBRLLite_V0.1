package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "TABLEVERSION")
@SqlResultSetMapping(
        name = "TablesByModuleMapping",
        entities = {
            @EntityResult(entityClass = TableVersionDPM.class),},
        columns = {
            @ColumnResult(name = "ModuleVId", type = Integer.class),}
)
public class TableVersionDPM implements Serializable {

    @Id
    @NotNull
    @Column(name = "TABLEVID")
    private int tableVID;

    @Column(name = "CODE")
    @NotNull
    @Size(max = 30)
    private String code;

    @Column(name = "NAME")
    @NotNull
    @Size(max = 255)
    private String name;

    @Column(name = "DESCRIPTION")
    //@Lob
    private String description;

    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name = "TABLEID", referencedColumnName = "TABLEID", nullable = false)
    private TableDPM table;

    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name = "ABSTRACTTABLEID", referencedColumnName = "TABLEID", nullable = false)
    private TableDPM abstractTable;

    @JoinColumn(referencedColumnName = "KEYID", name = "KEYID", nullable = false)
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private CompoundKey key;

    @JoinColumn(referencedColumnName = "PROPERTYID", name = "PROPERTYID", nullable = false)
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private Property property;

    @JoinColumn(referencedColumnName = "CONTEXTID", name = "CONTEXTID", nullable = false)
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private Context context;

    @JoinColumn(referencedColumnName = "RELEASEID", name = "STARTRELEASEID", nullable = false)
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private Release startRelease;

    @JoinColumn(referencedColumnName = "RELEASEID", name = "ENDRELEASEID")
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private Release endRelease;

    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)", nullable = false)
    //@OneToOne(fetch = FetchType.LAZY)
    @OneToOne
    private Concept concept;

    /*@OneToMany*/
    /*@JoinColumn(referencedColumnName = "KEYID", name = "KEYID", nullable = false)
    private List<KeyComposition> keyCompositionList;*/

    public TableVersionDPM() {
    }

    /*public List<KeyComposition> getKeyCompositionList() {
        return keyCompositionList;
    }

    public void setKeyCompositionList(List<KeyComposition> keyCompositionList) {
        this.keyCompositionList = keyCompositionList;
    }*/

    public void setTableVID(int tableVID) {
        this.tableVID = tableVID;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTable(TableDPM table) {
        this.table = table;
    }

    public void setAbstractTable(TableDPM abstractTable) {
        this.abstractTable = abstractTable;
    }

    public void setKey(CompoundKey key) {
        this.key = key;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setStartRelease(Release startRelease) {
        this.startRelease = startRelease;
    }

    public void setEndRelease(Release endRelease) {
        this.endRelease = endRelease;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public int getTableVID() {
        return tableVID;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TableDPM getTable() {
        return table;
    }

    public TableDPM getAbstractTable() {
        return abstractTable;
    }

    public CompoundKey getKey() {
        return key;
    }

    public Property getProperty() {
        return property;
    }

    public Context getContext() {
        return context;
    }

    public Release getStartRelease() {
        return startRelease;
    }

    public Release getEndRelease() {
        return endRelease;
    }

    public Concept getConcept() {
        return concept;
    }

}
