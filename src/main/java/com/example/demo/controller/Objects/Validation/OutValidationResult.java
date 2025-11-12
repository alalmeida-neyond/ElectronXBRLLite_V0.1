package com.example.demo.controller.Objects.Validation;

import java.io.Serializable;
import java.util.List;

import com.example.demo.DTOs.ValidationResultsDetailsDTO;
import com.example.demo.Data.Access.Info;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.Entities.DPMOrigin.OperationVersion;
import com.example.demo.controller.Objects.IO.IOState;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "OUT_VALIDATIONRESULT", schema = "DPM_ED")
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "ValidationResultsDetailsRow",
            classes = {
                @ConstructorResult(
                        targetClass = ValidationResultsDetailsDTO.class,
                        columns = {
                            @ColumnResult(name = "module", type = String.class),
                            @ColumnResult(name = "report", type = String.class),
                            @ColumnResult(name = "entity", type = String.class),
                            @ColumnResult(name = "domain", type = String.class),
                            @ColumnResult(name = "referenceDate", type = String.class),
                            @ColumnResult(name = "regraCode", type = String.class),
                            @ColumnResult(name = "severity", type = String.class),
                            @ColumnResult(name = "regraDomain", type = String.class),
                            @ColumnResult(name = "regra", type = String.class),
                            @ColumnResult(name = "regraExecutada", type = String.class),
                            @ColumnResult(name = "origem", type = String.class),
                            @ColumnResult(name = "resultado", type = String.class),
                            @ColumnResult(name = "dataProcessamento", type = String.class),
                            @ColumnResult(name = "difference", type = String.class),
                            @ColumnResult(name = "usedMargin", type = String.class)
                        }
                )
            })
    
})
public class OutValidationResult implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OUT_VALIDATIONRESULT_SEQ")
    @SequenceGenerator(name = "OUT_VALIDATIONRESULT_SEQ", sequenceName = "DPM_ED.OUT_VALIDATIONRESULT_SEQ", allocationSize = 1)
    @Column(name = "VALIDATIONRESULTID")
    @NotNull
    private Integer validationResultId;
    
    @JoinColumn(referencedColumnName = "OPERATIONVID", name = "OPERATIONVID")
    @ManyToOne(fetch = FetchType.LAZY)
    private OperationVersion operationVersion;
    
    @JoinColumn(referencedColumnName = "IO_STATEID", name = "STATEID")
    @ManyToOne(fetch = FetchType.LAZY)
    private IOState ioState;
    
    /*@OneToMany(cascade = CascadeType.ALL, mappedBy = "validationResult")
    private List<OutValidationResultDetails> listValidationResultDetails;
    */
    public OutValidationResult(){}
    
    public OutValidationResult(OperationVersion operation, IOState ioState){
        this.operationVersion = operation;
        this.ioState = ioState;
    }
    
    public void setIoState(List<OutValidationResultDetails> resultDetails) {
        if(resultDetails != null && !resultDetails.isEmpty()){
            int countOk = 0;
            int countDoNotRun = 0;
            int totalDetails = resultDetails.size();

            for(OutValidationResultDetails detail : resultDetails){
                if(detail.getIoState().getIoStateId() == Constants.RULEOK.getKey()){
                    countOk++;
                } else if (detail.getIoState().getIoStateId() == Constants.RULEDONOTRUN.getKey()){
                    countDoNotRun++;
                }
            }
            
            //incluir para ter o que é misto
            if (countDoNotRun == totalDetails){
                this.ioState = Info.getInstance().getIOStateByID(Constants.RULEDONOTRUN.getKey());//new IOState(Constants.RULEDONOTRUN.getKey(), new IOTypeState(Constants.RULEDONOTRUN.getValue()));
            } else if((countOk+countDoNotRun) == totalDetails){
                this.ioState = Info.getInstance().getIOStateByID(Constants.RULEOK.getKey());//new IOState(Constants.RULEOK.getKey(), new IOTypeState(Constants.RULEOK.getValue()));
            } else if ((countOk+countDoNotRun) == 0){
                this.ioState = Info.getInstance().getIOStateByID(Constants.RULENOTOK.getKey());//new IOState(Constants.RULENOTOK.getKey(), new IOTypeState(Constants.RULENOTOK.getValue()));
            } else {
                this.ioState = Info.getInstance().getIOStateByID(Constants.RULEOKWITHNOTOK.getKey());//new IOState(Constants.RULEOKWITHNOTOK.getKey(), new IOTypeState(Constants.RULEOK.getValue()));
            }
        }
    }
    
    public Integer getOperationVID() {
        return this.operationVersion.getOperationVID();
    }

    public Integer getValidationResultId() {
        return validationResultId;
    }

    public void setValidationResultId(Integer validationResultId) {
        this.validationResultId = validationResultId;
    }

    public OperationVersion getOperationVersion() {
        return operationVersion;
    }

    public void setOperationVersion(OperationVersion operationVersion) {
        this.operationVersion = operationVersion;
    }

    public IOState getIoState() {
        return ioState;
    }

    public void setIoState(IOState ioState) {
        this.ioState = ioState;
    }

    /*public List<OutValidationResultDetails> getListValidationResultDetails() {
        return listValidationResultDetails;
    }

    public void setListValidationResultDetails(List<OutValidationResultDetails> listValidationResultDetails) {
        this.listValidationResultDetails = listValidationResultDetails;
    }*/

    
}
