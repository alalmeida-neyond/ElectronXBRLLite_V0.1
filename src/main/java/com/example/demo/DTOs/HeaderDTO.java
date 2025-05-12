package com.example.demo.DTOs;

public class HeaderDTO {
    private int headerId;
    private String code;
    private char direction;

    public HeaderDTO(int headerId, String code, char direction) {
        this.headerId = headerId;
        this.code = code;
        this.direction = direction;
    }

    
    
    public int getHeaderId() {
        return headerId;
    }

    public void setHeaderId(int headerId) {
        this.headerId = headerId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public char getDirection() {
        return direction;
    }

    public void setDirection(char direction) {
        this.direction = direction;
    }
    
    
    
}
