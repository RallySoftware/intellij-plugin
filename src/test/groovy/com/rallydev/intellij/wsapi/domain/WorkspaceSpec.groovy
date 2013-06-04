package com.rallydev.intellij.wsapi.domain

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import spock.lang.Specification

class WorkspaceSpec extends Specification {

    def "properties properly assignable from json"() {
        given:
        JsonElement json  = new JsonParser().parse(RequirementSpec.classLoader.getResourceAsStream('workspace_single.json').text)

        when:
        Workspace workspace = new Workspace()
        workspace.assignProperties(json['QueryResult']['Results'].first())

        then:
        workspace.name == 'Workspace 1'
        workspace.objectID == '11864'
    }

}
