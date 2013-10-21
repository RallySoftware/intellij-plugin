package com.rallydev.intellij.wsapi.client

import com.rallydev.intellij.wsapi.domain.DomainObject

class PostRequest implements RallyRequest {

    DomainObject domainObject

    String getEncodedUrl() {
        return domainObject._ref
    }

}
