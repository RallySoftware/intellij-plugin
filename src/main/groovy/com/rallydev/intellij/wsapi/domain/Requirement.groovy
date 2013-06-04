package com.rallydev.intellij.wsapi.domain

import com.rallydev.intellij.wsapi.ApiEndpoint

class Requirement extends Artifact {
    static String TYPE = 'HierarchicalRequirement'

    String _type = TYPE

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.HIERARCHICAL_REQUIREMENT
    }

}
