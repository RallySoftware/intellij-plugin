package com.rallydev.intellij.wsapi;

//Written in Java due to http://jira.codehaus.org/browse/GROOVY-5212
public enum ApiEndpoint {
    ARTIFACT, DEFECT, HIERARCHICAL_REQUIREMENT, PROJECT, TYPE_DEFINITION, WORKSPACE;

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
