package com.rallydev.intellij.wsapi.domain

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import spock.lang.Specification


class AttributeDefinitionSpec extends Specification {

    def "allowed values is correctly parsed from json"() {
        given:
        JsonElement raw = new JsonParser().parse(
                AttributeDefinitionSpec.classLoader.getResourceAsStream('defect_typedef.json').text
        )

        JsonObject attributeRaw = (JsonObject) raw['QueryResult']['Results']['elements'][0]['Attributes'].elements.grep {
            it.members.find { key, value ->
                value.toString() == '"Environment"'
            }
        }[0]

        and:
        attributeRaw.add('Workspace', raw['QueryResult']['Results']['elements'][0]['Workspace'])

        and:
        AttributeDefinition attribute = new AttributeDefinition()
        attribute.assignProperties(attributeRaw)

        expect:
        attribute.allowedValues.size() == 8
        attribute.allowedValues.grep ''
        attribute.allowedValues.grep 'Test'
        attribute.allowedValues.grep 'Production'
        attribute.allowedValues.grep 'On-Premise Test'
        attribute.allowedValues.grep 'On Premise'
        attribute.allowedValues.grep 'Demo'
        attribute.allowedValues.grep 'Integration Test'
        attribute.allowedValues.grep 'Integration'
    }

}
