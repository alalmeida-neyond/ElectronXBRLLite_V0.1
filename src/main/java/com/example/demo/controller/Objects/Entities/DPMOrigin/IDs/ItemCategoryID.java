package com.example.demo.controller.Objects.Entities.DPMOrigin.IDs;

import java.io.Serializable;
import jakarta.persistence.*;

import java.util.Objects;

public class ItemCategoryID implements Serializable{
    
    
    @Column(name = "ITEMID")
    private int itemId;
    
    @Column(name = "STARTRELEASEID")
    private int startReleaseId;

    @Column(name = "CATEGORYID")
    private int categoryId;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getStartReleaseId() {
        return startReleaseId;
    }

    public void setStartReleaseId(int startReleaseId) {
        this.startReleaseId = startReleaseId;
    }
    
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemCategoryID)) return false;
        ItemCategoryID that = (ItemCategoryID) o;
        return itemId == that.itemId &&
               startReleaseId == that.startReleaseId &&
               categoryId == that.categoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, startReleaseId, categoryId);
    }
    
}
