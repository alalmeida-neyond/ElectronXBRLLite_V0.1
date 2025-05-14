/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "OPERATION")
public class Operation implements Serializable{
    
    @Id
    @NotNull
    @Column(name = "OPERATIONID")
    private int operationID;
    
    @Column(name = "CODE")
    @NotNull
    @Size(max = 20)
    private String code;
    
    @Column(name = "TYPE")
    @NotNull
    @Size(max = 20)
    private String type;
    
    @Column(name = "SOURCE")
    @NotNull
    @Size(max = 20)
    private String source;
    
    @JoinColumn(referencedColumnName = "OPERATIONID", name = "GROUPOPERID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Operation groupOper;
    
    @JoinColumn(referencedColumnName = "CONCEPTGUID", name = "ROWGUID", columnDefinition = "RAW(50)")
    @OneToOne(fetch = FetchType.LAZY)
    private Concept concept;

    public int getOperationID() {
        return operationID;
    }

    public void setOperationID(int operationID) {
        this.operationID = operationID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Operation getGroupOper() {
        return groupOper;
    }

    public void setGroupOper(Operation groupOper) {
        this.groupOper = groupOper;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
