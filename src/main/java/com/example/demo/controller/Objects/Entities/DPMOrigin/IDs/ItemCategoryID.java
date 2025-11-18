package com.example.demo.controller.Objects.Entities.DPMOrigin.IDs;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItemCategoryID implements Serializable{
    
    
    @Column(name = "ITEMID")
    private int itemId;
    
    @Column(name = "STARTRELEASEID")
    private int startReleaseId;

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
    
    
}
