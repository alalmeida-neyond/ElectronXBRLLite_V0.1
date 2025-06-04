package com.example.demo.controller.Objects.Validation;

import java.util.Objects;


public class ValMLKey {

    private Integer xIndex;
    private Integer yIndex;
    private Integer zIndex;

    public boolean isNull(){
        return this.xIndex == null && this.yIndex == null && this.zIndex == null;
    }
    
    public Integer getxIndex() {
        return xIndex;
    }

    public void setxIndex(Integer xIndex) {
        this.xIndex = xIndex;
    }

    public Integer getyIndex() {
        return yIndex;
    }

    public void setyIndex(Integer yIndex) {
        this.yIndex = yIndex;
    }

    public Integer getzIndex() {
        return zIndex;
    }

    public void setzIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.xIndex);
        hash = 79 * hash + Objects.hashCode(this.yIndex);
        hash = 79 * hash + Objects.hashCode(this.zIndex);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; 
        }
        
        if (obj == null){
            if(this.isNull()) return true;
        }
        
        if (obj != null && getClass() != obj.getClass()) {
            return false;
        }
        
        final ValMLKey other = (ValMLKey) obj;

        return Objects.equals(this.xIndex, other.xIndex)
                && Objects.equals(this.yIndex, other.yIndex)
                && Objects.equals(this.zIndex, other.zIndex);
    }

}
