package com.example.demo.controller.Objects.Validation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.example.demo.Converter.*;
import com.example.demo.Data.Access.Info;
import com.example.demo.Resources.Constants;
import com.example.demo.controller.Objects.IO.IOState;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "OUT_VALIDATIONRESULTDETAILS", schema = "DPM_OD")
public class OutValidationResultDetails implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OUT_VALIDATIONRESULTDETAILS_SEQ")
    @SequenceGenerator(name = "OUT_VALIDATIONRESULTDETAILS_SEQ", sequenceName = "DPM_OD.OUT_VALIDATIONRESULTDETAILS_SEQ", allocationSize = 1)
    @Column(name = "VALIDATIONRESULTDETAILSID")
    @NotNull
    private Integer validationResultDetailsId;
    
    @JoinColumn(referencedColumnName = "VALIDATIONRESULTID", name = "VALIDATIONRESULTID")
    @ManyToOne
    private OutValidationResult validationResult;
    
    @JoinColumn(referencedColumnName = "IO_STATEID", name = "STATEID")
    @ManyToOne
    private IOState ioState;
    
    @Column(name = "TIMESTAMP")
    @NotNull
    @Convert(converter = LocalDateTimePersistenceConverter.class)
    private LocalDateTime timeStampCreated;
    
    @Column(name = "EXPRESSION")
    @NotNull
    @Lob
    private String expression;
    
    @Column(name = "DIFFERENCE")
    private BigDecimal difference;
    
    @Column(name = "USEDMARGIN", columnDefinition = "CHAR(1)", nullable = false)
    private boolean usedMargin;
    
    @Column(name = "DOMAIN")
    @Lob
    private String domain;  
    
    public OutValidationResultDetails(){}
    
    public static List<OutValidationResultDetails> createResultDetails(List<ValResult> results, OutValidationResult outResult){
        List<OutValidationResultDetails> outResDetails = new ArrayList<>();
        
        try {
            if(results != null && !results.isEmpty()){
                for(ValResult result : results){
                    OutValidationResultDetails outResDetail = new OutValidationResultDetails();
                    
                    if(result.valueIsNull()){
                        outResDetail.ioState = Info.getInstance().getIOStateByID(Constants.RULEDONOTRUN.getKey());//new IOState(Constants.RULEDONOTRUN.getKey(), new IOTypeState(Constants.RULEDONOTRUN.getValue()));
                    } else if(result.getRawValue().equals(Constants.FALSERESULT)){
                        outResDetail.ioState = Info.getInstance().getIOStateByID(Constants.RULENOTOK.getKey());//new IOState(Constants.RULENOTOK.getKey(), new IOTypeState(Constants.RULENOTOK.getValue()));
                    } else if(result.getRawValue().equals(Constants.TRUERESULT)) {
                        outResDetail.ioState = Info.getInstance().getIOStateByID(Constants.RULEOK.getKey());//new IOState(Constants.RULEOK.getKey(), new IOTypeState(Constants.RULEOK.getValue()));
                    }
                    
                    if(result.getDifference() != null && outResDetail.ioState.getIoStateId() == Constants.RULENOTOK.getKey()){
                        outResDetail.difference = result.getDifference();
                    }
                    
                    outResDetail.usedMargin = result.isUsedMargin();
                    outResDetail.expression = result.getExpression();
                    outResDetail.timeStampCreated = LocalDateTime.now();
                    outResDetail.validationResult = outResult;
                    outResDetail.setDomain(result.getDomain());                     
                    outResDetails.add(outResDetail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return outResDetails;
    }

    public Integer getValidationResultDetailsId() {
        return validationResultDetailsId;
    }

    public void setValidationResultDetailsId(Integer validationResultDetailsId) {
        this.validationResultDetailsId = validationResultDetailsId;
    }

    public OutValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(OutValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public IOState getIoState() {
        return ioState;
    }

    public void setIoState(IOState ioState) {
        this.ioState = ioState;
    }

    public LocalDateTime getTimeStampCreated() {
        return timeStampCreated;
    }

    public void setTimeStampCreated(LocalDateTime timeStampCreated) {
        this.timeStampCreated = timeStampCreated;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }

    public boolean isUsedMargin() {
        return usedMargin;
    }

    public void setUsedMargin(boolean usedMargin) {
        this.usedMargin = usedMargin;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }    
    
    public void setDomain(List<String> domain){
        Set<String> domainWithoutDuplicatesOrdered = new TreeSet<>(domain);
        this.domain = domainWithoutDuplicatesOrdered.stream().collect(Collectors.joining(" | "));
    }
}
