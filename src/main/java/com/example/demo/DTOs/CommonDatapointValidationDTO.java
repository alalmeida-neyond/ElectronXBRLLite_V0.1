
package com.example.demo.DTOs;

public class CommonDatapointValidationDTO {
    
    private String details;
    private String domain;

    public CommonDatapointValidationDTO(String details, String domain) {
        this.details = details;
        this.domain = domain;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    
}
