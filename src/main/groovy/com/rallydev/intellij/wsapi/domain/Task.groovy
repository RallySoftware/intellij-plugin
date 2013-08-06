package com.rallydev.intellij.wsapi.domain

import com.rallydev.intellij.wsapi.ApiEndpoint

class Task extends Artifact {
    static final String TYPE = 'Task'

    String state
    String _type = TYPE

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.TASK
    }

}
