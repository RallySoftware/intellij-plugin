package com.rallydev.intellij.wsapi.domain

import com.google.gson.JsonObject
import com.rallydev.intellij.wsapi.ApiEndpoint

class Artifact extends DomainObject {

    String description
    String formattedID
    String name
    Date lastUpdateDate
    String _type

    String project

    @Override
    List<String> getExcludedProperties() {
        EXCLUDED_PROPERTIES + ['project']
    }

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.ARTIFACT
    }

    @Override
    void assignProperties(JsonObject raw) {
        super.assignProperties(raw)
        project = raw['Project']['_ref'].value
    }

}
