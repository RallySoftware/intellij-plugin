package com.rallydev.intellij.wsapi.domain

import com.rallydev.intellij.wsapi.ApiEndpoint

class TypeDefinition extends DomainObject {
    static final String TYPE = 'Type Definition'

    String displayName
    String IDPrefix
    String readOnly

    Collection<AttributeDefinition> attributeDefinitions

    @Override
    List<String> getExcludedProperties() {
        EXCLUDED_PROPERTIES + ['attributeDefinitions']
    }

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.TYPE_DEFINITION
    }

}
