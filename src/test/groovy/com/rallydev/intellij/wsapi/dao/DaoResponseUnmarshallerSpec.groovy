package com.rallydev.intellij.wsapi.dao

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.DomainObject
import com.rallydev.intellij.wsapi.domain.Requirement

class DaoResponseUnmarshallerSpec extends BaseContainerSpec {

    def setup() {
        registerComponentImplementation(DaoResponseUnmarshaller)
    }

    def "DI is correctly setup"() {
        when:
        DaoResponseUnmarshaller unmarshaller = ServiceManager.getService(DaoResponseUnmarshaller.class)

        then:
        unmarshaller
    }

    def "Un-marshall result from single domain object response as ApiResponse"() {
        given:
        DaoResponseUnmarshaller unmarshaller = ServiceManager.getService(DaoResponseUnmarshaller.class)
        ApiResponse apiResponse = new ApiResponse(DaoResponseUnmarshallerSpec.classLoader.getResourceAsStream('single_requirement.json').text)

        when:
        DomainObject domainObject = unmarshaller.buildDomainObject(Artifact, apiResponse)

        then:
        domainObject.class == Requirement
        domainObject.objectID == "14345"
        domainObject._type == 'HierarchicalRequirement'
    }

    def "Un-marshall result from domain objects in JSON"() {
        given:
        DaoResponseUnmarshaller unmarshaller = ServiceManager.getService(DaoResponseUnmarshaller.class)
        JsonElement parsedJson = new JsonParser().parse(DaoResponseUnmarshallerSpec.classLoader.getResourceAsStream('multiple_requirements.json').text)

        JsonObject singleResult = parsedJson['QueryResult']['Results'].elements[0]

        when:
        DomainObject domainObject = unmarshaller.buildDomainObject(Artifact, singleResult)

        then:
        domainObject.class == Requirement
        domainObject.objectID == "14345"
        domainObject._type == 'HierarchicalRequirement'
    }

    def "Un-marshall should not instantiate something that is not a subtype"() {
        given:
        DaoResponseUnmarshaller unmarshaller = ServiceManager.getService(DaoResponseUnmarshaller.class)
        ApiResponse apiResponse = new ApiResponse(DaoResponseUnmarshallerSpec.classLoader.getResourceAsStream('single_requirement.json').text)

        when:
        unmarshaller.buildDomainObject(Defect, apiResponse)

        then:
        thrown(IllegalArgumentException)
    }

}
