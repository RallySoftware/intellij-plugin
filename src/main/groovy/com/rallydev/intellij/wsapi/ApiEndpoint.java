package com.rallydev.intellij.wsapi;

import com.rallydev.intellij.wsapi.domain.Artifact;
import com.rallydev.intellij.wsapi.domain.AttributeDefinition;
import com.rallydev.intellij.wsapi.domain.Defect;
import com.rallydev.intellij.wsapi.domain.Project;
import com.rallydev.intellij.wsapi.domain.Requirement;
import com.rallydev.intellij.wsapi.domain.Task;
import com.rallydev.intellij.wsapi.domain.TypeDefinition;
import com.rallydev.intellij.wsapi.domain.Workspace;

import java.util.HashMap;
import java.util.Map;

//Written in Java due to http://jira.codehaus.org/browse/GROOVY-5212
public enum ApiEndpoint {
    ARTIFACT, ATTRIBUTE_DEFINITION, DEFECT, HIERARCHICAL_REQUIREMENT, PROJECT, TASK, TYPE_DEFINITION, WORKSPACE;

    public static final Map<Class, ApiEndpoint> DOMAIN_CLASS_ENDPOINTS = new HashMap<Class, ApiEndpoint>();

    static {
        DOMAIN_CLASS_ENDPOINTS.put(Artifact.class, ARTIFACT);
        DOMAIN_CLASS_ENDPOINTS.put(AttributeDefinition.class, ATTRIBUTE_DEFINITION);
        DOMAIN_CLASS_ENDPOINTS.put(Defect.class, DEFECT);
        DOMAIN_CLASS_ENDPOINTS.put(Requirement.class, HIERARCHICAL_REQUIREMENT);
        DOMAIN_CLASS_ENDPOINTS.put(Project.class, PROJECT);
        DOMAIN_CLASS_ENDPOINTS.put(Task.class, TASK);
        DOMAIN_CLASS_ENDPOINTS.put(TypeDefinition.class, TYPE_DEFINITION);
        DOMAIN_CLASS_ENDPOINTS.put(Workspace.class, WORKSPACE);
    }

    public static ApiEndpoint fromType(String type) {
        if (Defect.getTYPE().equals(type)) return DEFECT;
        if (Requirement.getTYPE().equals(type)) return HIERARCHICAL_REQUIREMENT;
        if (Task.getTYPE().equals(type)) return TASK;
        return null;
    }

    @Override
    public String toString() {
        return super.toString().replaceAll("_", "").toLowerCase();
    }

    public String getTypeDefinitionElementName() {
        switch (this) {
            case ATTRIBUTE_DEFINITION:
                return "AttributeDefinition";
            case DEFECT:
                return "Defect";
            case HIERARCHICAL_REQUIREMENT:
                return "HierarchicalRequirement";
            case PROJECT:
                return "Project";
            case TASK:
                return "Task";
            case TYPE_DEFINITION:
                return "TypeDefinition";
            case WORKSPACE:
                return "Workspace";
            default:
                throw new IllegalArgumentException("No type definition elementName for " + this);
        }
    }

    public Class getDomainClass() {
        switch (this) {
            case DEFECT:
                return Defect.class;
            case HIERARCHICAL_REQUIREMENT:
                return Requirement.class;
            case PROJECT:
                return Project.class;
            case TASK:
                return Task.class;
            case TYPE_DEFINITION:
                return TypeDefinition.class;
            case WORKSPACE:
                return Workspace.class;
            default:
                throw new IllegalArgumentException("No domain class for " + this);
        }
    }

    public String getJsonRoot() {
        String jsonRoot = "";
        for (String word : super.toString().split("_")) {
            jsonRoot += word.substring(0, 1) + word.toLowerCase().substring(1);
        }
        return jsonRoot;
    }

}
