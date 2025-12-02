package com.example.demo.controller.Objects.Entities.Conf;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.Converter.LocalDatePersistenceConverter;
import com.example.demo.Converter.LocalDateTimePersistenceConverter;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "CONF_IMPORTRULES", schema = "DPM_OD")
@NamedQuery(name = "ConfImportRules.findAll", query = "SELECT e FROM ConfImportRules e order by e.ruleName")
@SqlResultSetMapping(
        name = "ConfImportRulesWithDataType",
        entities = {
            @EntityResult(entityClass = ConfImportRules.class),
        },
        columns = {
            @ColumnResult(name = "dataType", type = String.class)
        }
)
public class ConfImportRules {
    
    @Id
    @Column(name = "IMPORTRULESID")
    private Integer importRuleID;
    
    @Column(name = "RULENAME")
    @NotNull
    private String ruleName;    
    
    @Column(name = "RULEDESCRIPTION")
    private String ruleDescription;
    
    @Column(name = "CREATIONDATE")
    @NotNull
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime creationDate;
    
    @Column(name = "USERID")
    @NotNull
    private String userid;
    
    @Column(name = "FROMDATE")
    @NotNull
    @Convert(converter = LocalDatePersistenceConverter.class)    
    private LocalDate fromdate;
    
    @Column(name = "TODATE")
    @NotNull
    @Convert(converter = LocalDatePersistenceConverter.class)
    private LocalDate todate;
    
    @Column(name = "ALWAYSRUN", columnDefinition = "NUMBER(1)")
    @NotNull
    private boolean alwaysRun;
    
    
    @Column(name = "IMPORTASSOCIATIONKEYID")
    @NotNull
    private int importAssociationKeyID;

    public ConfImportRules() {
    }

    public Integer getImportRuleID() {
        return importRuleID;
    }

    public void setImportRuleID(Integer importRuleID) {
        this.importRuleID = importRuleID;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public LocalDate getFromdate() {
        return fromdate;
    }

    public void setFromdate(LocalDate fromdate) {
        this.fromdate = fromdate;
    }

    public LocalDate getTodate() {
        return todate;
    }

    public void setTodate(LocalDate todate) {
        this.todate = todate;
    }

    public boolean isAlwaysRun() {
        return alwaysRun;
    }

    public void setAlwaysRun(boolean alwaysRun) {
        this.alwaysRun = alwaysRun;
    }

    public int getImportAssociationKeyID() {
        return importAssociationKeyID;
    }

    public void setImportAssociationKeyID(int importAssociationKeyID) {
        this.importAssociationKeyID = importAssociationKeyID;
    }
    
}
