package com.rallydev.intellij.wsapi.domain

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.rallydev.intellij.wsapi.ApiEndpoint

class AttributeDefinition extends Artifact {
    static final String TYPE = 'Attribute Definition'

    String state
    String _type = TYPE
    List<String> allowedValues = new LinkedList<String>()

    @Override
    ApiEndpoint getApiEndpoint() {
        return ApiEndpoint.ATTRIBUTE_DEFINITION
    }

    @Override
    protected void assignProperty(MetaProperty property, JsonElement json) {
        if(property.name == 'allowedValues') {
            JsonArray jsonArray = (JsonArray) json
            jsonArray.each {
                allowedValues << it['StringValue']['value']
            }
        } else {
            super.assignProperty(property, json)
        }
    }

    @Override
    String toString() {
        "${getClass().simpleName} '${name}' [${objectID}]"
    }

}
