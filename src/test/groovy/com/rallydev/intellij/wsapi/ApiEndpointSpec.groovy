package com.rallydev.intellij.wsapi

import spock.lang.Specification

import static com.rallydev.intellij.wsapi.ApiEndpoint.ARTIFACT
import static com.rallydev.intellij.wsapi.ApiEndpoint.HIERARCHICAL_REQUIREMENT
import static com.rallydev.intellij.wsapi.ApiEndpoint.TYPE_DEFINITION

class ApiEndpointSpec extends Specification {

    def "toString should return single lowercase word"() {
        expect:
        endpoint.toString() == expectedValue

        where:
        endpoint                 | expectedValue
        ARTIFACT                 | 'artifact'
        HIERARCHICAL_REQUIREMENT | 'hierarchicalrequirement'
        TYPE_DEFINITION          | 'typedefinition'
    }

    def "json root returns camelCase"() {
        expect:
        endpoint.jsonRoot == expectedValue

        where:
        endpoint                 | expectedValue
        ARTIFACT                 | 'Artifact'
        HIERARCHICAL_REQUIREMENT | 'HierarchicalRequirement'
        TYPE_DEFINITION          | 'TypeDefinition'
    }

}
