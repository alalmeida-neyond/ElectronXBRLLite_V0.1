/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "OPERATIONVERSION")
public class OperationVersion implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "OPERATIONVID")
    private int operationVID;
    
    @JoinColumn(referencedColumnName = "OPERATIONID", name = "OPERATIONID", nullable = false)
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private Operation operation;
    
    @JoinColumn(referencedColumnName = "OPERATIONVID", name = "PRECONDITIONOPERATIONVID")
    @ManyToOne
    private OperationVersion preConditionOperationVersion;
    
    @JoinColumn(referencedColumnName = "OPERATIONVID", name = "SEVERITYOPERATIONVID")
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private OperationVersion severityOperationVersion;
    
    @JoinColumn(referencedColumnName = "RELEASEID", name = "STARTRELEASEID", nullable = false)
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private Release startRelease;
    
    @JoinColumn(referencedColumnName = "RELEASEID", name = "ENDRELEASEID")
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    private Release endRelease;
    
    @Column(name = "EXPRESSION")
    @NotNull
    @Lob
    private String expression;
    
    @Column(name = "DESCRIPTION")
    @Lob
    private String description;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    //@OneToOne(fetch = FetchType.LAZY)
    @OneToOne
    private Concept concept;
    
    @Column(name = "ENDORSEMENT")
    @Size(max = 255)
    private String endorsment;
    
    @Column(name = "ISVARIANTAPPROVED", columnDefinition = "CHAR(1)")
    private Boolean isVariantApproved;

    public int getOperationVID() {
        return operationVID;
    }

    public void setOperationVID(int operationVID) {
        this.operationVID = operationVID;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public OperationVersion getPreConditionOperationVersion() {
        return preConditionOperationVersion;
    }

    public void setPreConditionOperationVersion(OperationVersion preConditionOperationVersion) {
        this.preConditionOperationVersion = preConditionOperationVersion;
    }

    public OperationVersion getSeverityOperationVersion() {
        return severityOperationVersion;
    }

    public void setSeverityOperationVersion(OperationVersion severityOperationVersion) {
        this.severityOperationVersion = severityOperationVersion;
    }

    public Release getStartRelease() {
        return startRelease;
    }

    public void setStartRelease(Release startRelease) {
        this.startRelease = startRelease;
    }

    public Release getEndRelease() {
        return endRelease;
    }

    public void setEndRelease(Release endRelease) {
        this.endRelease = endRelease;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public String getEndorsment() {
        return endorsment;
    }

    public void setEndorsment(String endorsment) {
        this.endorsment = endorsment;
    }

    public boolean isIsVariantApproved() {
        return isVariantApproved;
    }

    public void setIsVariantApproved(boolean isVariantApproved) {
        this.isVariantApproved = isVariantApproved;
    }
    
    
}
