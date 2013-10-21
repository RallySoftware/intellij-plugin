package com.rallydev.intellij.wsapi.dao

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.DomainObject

class DaoResponseUnmarshaller {

    public static DaoResponseUnmarshaller getInstance() {
        return ServiceManager.getService(DaoResponseUnmarshaller.class)
    }

    DomainObject buildDomainObject(Class originDomainClass, ApiResponse response) {
        JsonElement json = response.json
        Map.Entry firstItem = json.entrySet().first()
        String elementName = firstItem.key as String
        DomainObject domainObject = getDomainObject(elementName, originDomainClass)

        JsonObject root = (JsonObject) firstItem.value
        domainObject.assignProperties(root)

        return domainObject
    }

    DomainObject buildDomainObject(Class originDomainClass, JsonObject json) {
        String elementName = json['_type']?.value
        DomainObject domainObject = getDomainObject(elementName, originDomainClass)

        domainObject.assignProperties(json)

        return domainObject
    }

    private DomainObject getDomainObject(String elementName, Class originDomainClass) {
        DomainObject domainObject = getDomainClassFromType(elementName).newInstance()
        if (!originDomainClass.isAssignableFrom(domainObject.class)) {
            throw new IllegalArgumentException("${originDomainClass} is not a ${originDomainClass} (element: ${elementName})")
        }
        return domainObject
    }


    private Class<? extends DomainObject> getDomainClassFromType(String type) {
        Class desiredType = null
        (ApiEndpoint.values() - ApiEndpoint.ARTIFACT).each { apiEndpoint ->
            if(apiEndpoint.typeDefinitionElementName == type) {
                desiredType = apiEndpoint.domainClass
            }
        }
        return desiredType ?: Artifact
    }

}
