package com.rallydev.intellij.wsapi.domain

import com.rallydev.intellij.wsapi.ApiEndpoint

class Defect extends Artifact {
    static final String TYPE = 'Defect'

    String state
    String _type = TYPE

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.DEFECT
    }

}
