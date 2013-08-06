package com.rallydev.intellij.wsapi

import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import com.rallydev.intellij.wsapi.domain.Task
import spock.lang.Specification

import static com.rallydev.intellij.wsapi.ApiEndpoint.ARTIFACT
import static com.rallydev.intellij.wsapi.ApiEndpoint.DEFECT
import static com.rallydev.intellij.wsapi.ApiEndpoint.HIERARCHICAL_REQUIREMENT
import static com.rallydev.intellij.wsapi.ApiEndpoint.TASK

class ApiEndpointSpec extends Specification {

    def "toString should return single lowercase word"() {
        expect:
        endpoint.toString() == expectedValue

        where:
        endpoint                 | expectedValue
        ARTIFACT                 | 'artifact'
        HIERARCHICAL_REQUIREMENT | 'hierarchicalrequirement'
    }

    def "json root returns camelCase"() {
        expect:
        endpoint.jsonRoot == expectedValue

        where:
        endpoint                 | expectedValue
        ARTIFACT                 | 'Artifact'
        HIERARCHICAL_REQUIREMENT | 'HierarchicalRequirement'
    }

    def "endpoints map returns endpoint from class"() {
        expect:
        ApiEndpoint.DOMAIN_CLASS_ENDPOINTS[cls] == expectedValue

        where:
        cls         | expectedValue
        Artifact    | ARTIFACT
        Requirement | HIERARCHICAL_REQUIREMENT
        Task        | TASK
    }

    def "fromType returns proper endpoint"() {
        expect:
        ApiEndpoint.fromType(type) == expectedValue

        where:
        type             | expectedValue
        Defect.TYPE      | DEFECT
        Requirement.TYPE | HIERARCHICAL_REQUIREMENT
        Task.TYPE        | TASK
    }

}
