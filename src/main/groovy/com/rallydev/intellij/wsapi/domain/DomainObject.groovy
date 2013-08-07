package com.rallydev.intellij.wsapi.domain

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
        ['class', 'apiEndpoint', 'excludedProperties', 'raw']

    String objectID
    Date creationDate
    String _ref

    JsonObject raw

    List<String> getExcludedProperties() {
        EXCLUDED_PROPERTIES
    }

    void assignProperties(JsonObject raw) {
        this.raw = raw

        List<MetaProperty> assignableProperties = metaClass.properties.findAll {
            !excludedProperties.contains(it.name) && !Modifier.isStatic(it.modifiers)
        }

        assignableProperties.each { MetaProperty property ->
            if (raw[property.name.capitalize()]) {
                assignProperty(property, (JsonElement) raw[property.name.capitalize()])
            }
        }
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
