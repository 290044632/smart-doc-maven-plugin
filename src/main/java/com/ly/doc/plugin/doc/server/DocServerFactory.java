package com.ly.doc.plugin.doc.server;

import com.ly.doc.model.ApiConfig;
import com.ly.doc.plugin.config.ApiConfigWrapper;
import com.ly.doc.plugin.config.DocServerConfig;
import com.ly.doc.plugin.doc.server.ftp.DocFtpServer;
import org.apache.maven.project.MavenProject;

public class DocServerFactory {
    private ApiConfig apiConfig;
    private MavenProject mavenProject;

    public DocServerFactory(ApiConfig apiConfig, MavenProject mavenProject) {
        this.apiConfig = apiConfig;
        this.mavenProject = mavenProject;
    }

    public IDocServer getDocServer() {
        ApiConfigWrapper apiConfigWrapper = (ApiConfigWrapper) this.apiConfig;
        DocServerConfig docServerConfig = apiConfigWrapper.getDocServerConfig();
        if (docServerConfig == null) {
            return () -> {
            };
        }
        switch (docServerConfig.getType()) {
            case "ftp":
            case "FTP":
                return new DocFtpServer(apiConfigWrapper, mavenProject);
            default:
                return () -> {
                };
        }
    }
}
