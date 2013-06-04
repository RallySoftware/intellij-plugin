package com.rallydev.intellij.wsapi.domain

import com.rallydev.intellij.wsapi.ApiEndpoint

class Artifact extends DomainObject {

    String description
    String formattedID
    String name
    Date lastUpdateDate
    String _type

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.ARTIFACT
    }

}
