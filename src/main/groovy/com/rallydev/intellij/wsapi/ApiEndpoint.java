package com.rallydev.intellij.wsapi;

import com.rallydev.intellij.wsapi.domain.Artifact;
import com.rallydev.intellij.wsapi.domain.Defect;
import com.rallydev.intellij.wsapi.domain.Project;
import com.rallydev.intellij.wsapi.domain.Requirement;
import com.rallydev.intellij.wsapi.domain.Workspace;

import java.util.HashMap;
import java.util.Map;

//Written in Java due to http://jira.codehaus.org/browse/GROOVY-5212
public enum ApiEndpoint {
    ARTIFACT, DEFECT, HIERARCHICAL_REQUIREMENT, PROJECT, WORKSPACE;

    public static final Map<Class, ApiEndpoint> DOMAIN_CLASS_ENDPOINTS = new HashMap<Class, ApiEndpoint>();

    static {
        DOMAIN_CLASS_ENDPOINTS.put(Artifact.class, ARTIFACT);
        DOMAIN_CLASS_ENDPOINTS.put(Defect.class, DEFECT);
        DOMAIN_CLASS_ENDPOINTS.put(Requirement.class, HIERARCHICAL_REQUIREMENT);
        DOMAIN_CLASS_ENDPOINTS.put(Project.class, PROJECT);
        DOMAIN_CLASS_ENDPOINTS.put(Workspace.class, WORKSPACE);
    }

    @Override
    public String toString() {
        return super.toString().replaceAll("_", "").toLowerCase();
    }

    public String getJsonRoot() {
        String jsonRoot = "";
        for (String word : super.toString().split("_")) {
            jsonRoot += word.substring(0, 1) + word.toLowerCase().substring(1);
        }
        return jsonRoot;
    }

}
