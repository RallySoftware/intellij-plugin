package com.rallydev.intellij.wsapi.domain

import spock.lang.Specification


class TypeDefinitionSpec extends Specification {

    def "findAttributeDefinitions returns attribute definition"() {
        given:
        AttributeDefinition scheduleStateAttributeDefinition = new AttributeDefinition(elementName: "scheduleState", name: "Schedule State", formattedID: "2")

        TypeDefinition typeDefinition = new TypeDefinition(
                attributeDefinitions: [
                        new AttributeDefinition(name: "state", formattedID: "1"),
                        scheduleStateAttributeDefinition,
                        new AttributeDefinition(name: "description", formattedID: "3")
                ]
        )

        expect:
        scheduleStateAttributeDefinition == typeDefinition.findAttributeDefinition(scheduleStateAttributeDefinition.elementName)
        !typeDefinition.findAttributeDefinition("foobar")
    }

}