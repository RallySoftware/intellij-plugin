package com.rallydev.intellij.wsapi.domain

import com.rallydev.intellij.wsapi.ApiEndpoint

class Project extends DomainObject {

    String name

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.PROJECT
    }

}
