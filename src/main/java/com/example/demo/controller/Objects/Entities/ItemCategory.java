package com.example.demo.controller.Objects.Entities;

import java.io.Serializable;
import java.util.UUID;

import com.example.demo.DTOs.DatapointItensDTO;
import jakarta.persistence.*;

import jakarta.validation.constraints.*;

import org.springframework.data.annotation.Immutable;

@Immutable
@Entity
@Table(name = "ITEMCATEGORY")
@SqlResultSetMapping(
        name = "DatapointItensDTO",
        classes = @ConstructorResult(
                targetClass = DatapointItensDTO.class,
                columns = { 
                            @ColumnResult(name = "xbrlHeader", type = String.class), 
                            @ColumnResult(name = "HeaderCode", type = String.class), 
                            @ColumnResult(name = "ValueCode", type = String.class),
                            @ColumnResult(name = "signature", type = String.class), 
                            @ColumnResult(name = "name", type = String.class)}))
public class ItemCategory implements Serializable{
    
    @Id
    private ItemCategoryID itemCategoryID;
    
    @MapsId("itemId")
    @JoinColumn(referencedColumnName = "ITEMID", name = "ITEMID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;
    
    @MapsId("startReleaseId")
    @JoinColumn(referencedColumnName = "RELEASEID", name = "STARTRELEASEID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Release startRelease;
    
    @MapsId("categoryId")
    @JoinColumn(referencedColumnName = "CATEGORYID", name = "CATEGORYID", nullable = false, insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
    
    @Column(name = "CODE")
    @NotNull
    @Size(max = 20)
    private String code;
    
    @Column(name = "ISDEFAULTITEM", columnDefinition = "CHAR(1)")
    @NotNull
    private boolean isDefaultItem;
    
    @Column(name = "SIGNATURE", unique = true)
    @NotNull
    @Size(max = 255)
    private String signature;
    
    @JoinColumn(referencedColumnName = "RELEASEID", name = "ENDRELEASEID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Release endRelease;
    
    @Column(name = "ROWGUID", columnDefinition = "RAW(50)")
    private UUID rowGUID;

    public ItemCategoryID getItemCategoryID() {
        return itemCategoryID;
    }

    public void setItemCategoryID(ItemCategoryID itemCategoryID) {
        this.itemCategoryID = itemCategoryID;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Release getStartRelease() {
        return startRelease;
    }

    public void setStartRelease(Release startRelease) {
        this.startRelease = startRelease;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isIsDefaultItem() {
        return isDefaultItem;
    }

    public void setIsDefaultItem(boolean isDefaultItem) {
        this.isDefaultItem = isDefaultItem;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Release getEndRelease() {
        return endRelease;
    }

    public void setEndRelease(Release endRelease) {
        this.endRelease = endRelease;
    }

    public UUID getRowGUID() {
        return rowGUID;
    }

    public void setRowGUID(UUID rowGUID) {
        this.rowGUID = rowGUID;
    }

    //Check if the desagregation is valid and return the correctCode
    public String checkIfDesagregationIsValid(String desagregationCode){
        if(getCode().toUpperCase().equals(desagregationCode.toUpperCase())){
            return getSignature();
        }
        return null;
    }

    
}
