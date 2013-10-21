package com.rallydev.intellij.wsapi.domain

import com.google.gson.JsonObject
import com.rallydev.intellij.wsapi.ApiEndpoint

import java.text.DateFormat

class Artifact extends DomainObject {

    String formattedID
    String name
    Date lastUpdateDate
    String description
    String notes

    String project
    String projectName

    @Override
    List<String> getExcludedProperties() {
        EXCLUDED_PROPERTIES + ['project', 'projectName']
    }

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.ARTIFACT
    }

    @Override
    void assignProperties(JsonObject raw) {
        super.assignProperties(raw)
        project = raw['Project']?.'_ref'?.value
        projectName = raw['Project']?.'_refObjectName'?.value
    }

    String getFormattedLastUpdateDate() {
        if (lastUpdateDate) {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lastUpdateDate)
        }
        return ''
    }

}
