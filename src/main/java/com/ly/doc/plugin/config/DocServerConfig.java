package com.ly.doc.plugin.config;

public class DocServerConfig {
    private String type = "none";
    private FtpServerConfig ftpConfig;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FtpServerConfig getFtpConfig() {
        return ftpConfig;
    }

    public void setFtpConfig(FtpServerConfig ftpConfig) {
        this.ftpConfig = ftpConfig;
    }
}
