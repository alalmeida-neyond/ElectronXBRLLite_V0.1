package com.example.demo.controller.Objects.Entities.DPMOrigin;

import java.io.Serializable;

import com.example.demo.DTOs.DataTypeHasUnitDTO;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "DATATYPE")
@NamedQuery(name="DataType.findAll", query="SELECT d FROM DataType d")
@SqlResultSetMapping(
    name = "dataTypeHasUnitDTOMapping",
    classes = {
        @ConstructorResult(
                targetClass = DataTypeHasUnitDTO.class,
                columns = {
                    @ColumnResult(name = "datatypeId", type = int.class),
                    @ColumnResult(name = "hasUnit", type = String.class),
                }
        )
    }
)
public class DataType implements Serializable {
    
    @Id
    @NotNull
    @Column(name = "DATATYPEID")
    private int dataTypeId;
    
    @Column(name = "CODE", unique = true)
    @NotNull
    @Size(max = 20)
    private String code;
    
    @Column(name = "\"NAME\"", unique = true)
    @NotNull
    @Size(max = 50)
    private String name;
    
    @JoinColumn(referencedColumnName = "DATATYPEID", name = "PARENTDATATYPEID")
    @ManyToOne(fetch = FetchType.LAZY)
    private DataType parentDataType;
    
    @Column(name = "ISACTIVE", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isActive;

    public int getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(int dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getParentDataType() {
        return parentDataType;
    }

    public void setParentDataType(DataType parentDataType) {
        this.parentDataType = parentDataType;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    
}
