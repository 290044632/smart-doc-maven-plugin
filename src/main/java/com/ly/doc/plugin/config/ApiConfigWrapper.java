package com.ly.doc.plugin.config;

import com.ly.doc.model.ApiConfig;

import javax.print.Doc;

public class ApiConfigWrapper extends ApiConfig {
    private DocServerConfig docServerConfig;

    public DocServerConfig getDocServerConfig() {
        return docServerConfig;
    }

    public void setDocServerConfig(DocServerConfig docServerConfig) {
        this.docServerConfig = docServerConfig;
    }
}
