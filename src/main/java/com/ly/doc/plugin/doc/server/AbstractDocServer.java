package com.ly.doc.plugin.doc.server;

import com.ly.doc.model.ApiConfig;
import com.ly.doc.plugin.config.ApiConfigWrapper;
import org.apache.maven.project.MavenProject;

public abstract class AbstractDocServer implements IDocServer {
    protected ApiConfigWrapper apiConfig;
    protected MavenProject mavenProject;

    public AbstractDocServer(ApiConfigWrapper apiConfigWrapper, MavenProject mavenProject) {
        this.apiConfig = apiConfigWrapper;
        this.mavenProject = mavenProject;
    }

    protected abstract void runWithArg(ApiConfigWrapper apiConfigWrapper, MavenProject mavenProject);

    @Override
    public void run() {
        this.runWithArg(apiConfig, mavenProject);
    }

    public ApiConfigWrapper getApiConfigWrapper() {
        return apiConfig;
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }
}
