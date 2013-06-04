package com.rallydev.intellij.wsapi.domain

import com.rallydev.intellij.wsapi.ApiEndpoint

class Workspace extends DomainObject {
    String name

    List<String> defectStates
    List<String> requirementStates

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.WORKSPACE
    }

}
