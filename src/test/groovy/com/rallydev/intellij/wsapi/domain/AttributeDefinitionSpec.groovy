package com.rallydev.intellij.wsapi.domain

import spock.lang.Specification


class AttributeDefinitionSpec extends Specification {

    def "allowed values is correctly parsed from json"() {
        expect:
        !'Implement'
    }

}
