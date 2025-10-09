package com.example.demo.DTOs;

import com.example.demo.Resources.Constants;

public class DatapointItensDTO {

    private String XBRLHeader;
    private String headerCode;
    private String valueCode;
    private String signature;
    private String name;

    public DatapointItensDTO(String XBRLHeader, String headerCode, String valueCode, String signature, String name) {
        this.XBRLHeader = XBRLHeader;
        this.headerCode = headerCode;
        this.valueCode = valueCode;
        this.signature = signature;
        this.name = name;
    }

    public boolean matchesWithThisItem(String valueInserted, boolean isDesagCodeFixed) {
        if (isDesagCodeFixed) {
            return this.headerCode != null && this.headerCode.trim().equalsIgnoreCase(valueInserted);
        }

        return (this.valueCode != null && this.valueCode.trim().equalsIgnoreCase(valueInserted))
                || (this.signature != null && this.signature.equalsIgnoreCase(Constants.EBASEPARATOR + valueInserted))
                || (this.signature != null && this.signature.equalsIgnoreCase(valueInserted))
                || (this.name != null && this.name.trim().equalsIgnoreCase(valueInserted));
    }

    public String getXBRLHeader() {
        return XBRLHeader;
    }

    public void setXBRLHeader(String XBRLHeader) {
        this.XBRLHeader = XBRLHeader;
    }

    public String getHeaderCode() {
        return headerCode;
    }

    public void setHeaderCode(String headerCode) {
        this.headerCode = headerCode;
    }

    public String getValueCode() {
        return valueCode;
    }

    public void setValueCode(String valueCode) {
        this.valueCode = valueCode;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
