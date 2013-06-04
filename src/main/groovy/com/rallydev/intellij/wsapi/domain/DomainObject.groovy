package com.rallydev.intellij.wsapi.domain

import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.ApiResponse

import java.text.SimpleDateFormat

abstract class DomainObject {
    private static final Logger log = Logger.getInstance(DomainObject)

    String objectID
    Date creationDate
    String _ref

    JsonObject raw

    DomainObject() {}

    void assignProperties(JsonObject raw) {
        this.raw = raw

        List<MetaProperty> assignableProperties = metaClass.properties.findAll {
            !['class', 'apiEndpoint'].contains(it.name)
        }

        assignableProperties.each { MetaProperty property ->
            if (raw[property.name.capitalize()]) {
                switch (property.type) {
                    case Date:
                        property.setProperty(this, parseWsapiDate((String) raw[property.name.capitalize()].value))
                        break
                    case String:
                    case Number:
                        property.setProperty(this, raw[property.name.capitalize()].value)
                        break
                    default:
                        log.debug("Property has class non-primitive/date class [${property}]")
                }
            }
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
        "${getClass().simpleName} [objectID]"
    }

    abstract ApiEndpoint getApiEndpoint();

}
