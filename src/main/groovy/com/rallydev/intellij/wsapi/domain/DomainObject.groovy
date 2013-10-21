package com.rallydev.intellij.wsapi.domain

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.ApiResponse

import java.lang.reflect.Modifier
import java.text.SimpleDateFormat

abstract class DomainObject {
    private static final Logger log = Logger.getInstance(DomainObject)
    protected static final List<String> EXCLUDED_PROPERTIES =
        ['class', 'apiEndpoint', 'excludedProperties', 'workspaceRef', 'raw']

    String objectID
    Date creationDate
    String _ref

    String workspaceRef

    JsonObject raw

    List<String> getExcludedProperties() {
        EXCLUDED_PROPERTIES
    }

    void assignProperties(JsonObject raw) {
        this.raw = raw

        List<MetaProperty> assignableProperties = metaClass.properties.findAll {
            !excludedProperties.contains(it.name) && !Modifier.isStatic(it.modifiers)
        }

        assignWorkspaceRef(raw)
        assignableProperties.each { MetaProperty property ->
            if (raw[property.name.capitalize()]) {
                assignProperty(property, (JsonElement) raw[property.name.capitalize()])
            }
        }
    }

    String asJson() {
        List<MetaProperty> serializedProperties = metaClass.properties.findAll {
            !excludedProperties.contains(it.name) && !Modifier.isStatic(it.modifiers)
        }

        Map json = [:]
        serializedProperties.each {
            json[it.name.capitalize()] = it.getProperty(this)
        }

        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create()
                .toJson(json)
    }

    protected void assignWorkspaceRef(JsonObject raw) {
        workspaceRef = raw.Workspace?._ref
    }

    protected void assignProperty(MetaProperty property, JsonElement json) {
        switch (property.type) {
            case Date:
                property.setProperty(this, parseWsapiDate((String) json['value']))
                break
            case String:
            case Number:
                property.setProperty(this, json['value'])
                break
            default:
                log.debug("Property has class non-primitive/date class [${property}]")
        }
    }

    private static Date parseWsapiDate(String rawDate) {
        try {
            return new SimpleDateFormat(ApiResponse.RALLY_DATE_FORMAT).parse(rawDate)
        } catch (Exception e) {
            return null
        }
    }

    @Override
    String toString() {
        "${getClass().simpleName} [${objectID}]"
    }

    abstract ApiEndpoint getApiEndpoint()

}
