package com.example.demo.DTOs;

public class DownloadRequest {
    private Integer templateId;
    private String serverFilename; 

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public String getServerFilename() { return serverFilename; }
    public void setServerFilename(String serverFilename) { this.serverFilename = serverFilename; }
}
