package com.rallydev.intellij.wsapi.domain

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import spock.lang.Specification

class RequirementSpec extends Specification {

    def "properties properly assignable from json"() {
        given:
        JsonElement json  = new JsonParser().parse(RequirementSpec.classLoader.getResourceAsStream('single_requirement.json').text)

        when:
        Requirement requirement = new Requirement()
        requirement.assignProperties(json['HierarchicalRequirement'])

        then:
        requirement.objectID == '14345'
        requirement._ref == 'http://localhost:7001/slm/webservice/1.39/hierarchicalrequirement/14345.js'
        requirement.creationDate == new Date((new Date(2012 - 1900, 10, 21, 6, 7, 34).getTime()) + 25) //deal with 127 milliseconds

        requirement.formattedID == 'US1'
        requirement.description == '<p>Allow potential Rally customers to sign up for a trial that is easy to understand.</p>'
        requirement.name == 'lone story'
        requirement.lastUpdateDate == new Date((new Date(2012 - 1900, 10, 21, 8, 0, 0).getTime()) + 127) //deal with 127 milliseconds
    }

}
