package com.rallydev.intellij.task

import com.intellij.tasks.TaskType
import com.rallydev.intellij.SpecUtils
import com.rallydev.intellij.task.RallyTask
import com.rallydev.intellij.task.WsapiQuery
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.GetRequest
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import spock.lang.Shared
import spock.lang.Specification

class WsapiQuerySpec extends Specification {

    static String server = 'http://asdf'

    @Shared
    RallyClient recordingClient
    List<String> requests = []

    def setup() {
        recordingClient = Mock(RallyClient)
        recordingClient.makeRequest(_ as GetRequest) >> { GetRequest request ->
            requests << request.getUrl(server.toURL())
            return new ApiResponse(SpecUtils.minimalResponseJson)
        }
    }


    def "findTasks returns list of tasks from requirements and defects"() {
        given:
        WsapiQuery query = new WsapiQuery(recordingClient)
        query.defectDao = Mock(GenericDao, constructorArgs: [recordingClient, Defect])
        query.requirementDao = Mock(GenericDao, constructorArgs: [recordingClient, Defect])

        and:
        1 * query.defectDao.find(_, _) >> {
            [new Defect(objectID: 'D1', _type: 'Defect')]
        }
        1 * query.requirementDao.find(_, _) >> {
            [new Requirement(objectID: 'R1', _type: 'HierarchicalRequirement')]
        }

        when:
        List<RallyTask> tasks = query.findTasks('someQuery', 50, 0)

        then:
        tasks.size() == 2

        and:
        tasks[0].id == 'D1'
        tasks[0].type == TaskType.BUG

        and:
        tasks[1].id == 'R1'
        tasks[1].type == TaskType.FEATURE
    }

    def "findTasks returns single task from artifact"() {
        given:
        WsapiQuery query = new WsapiQuery(recordingClient)
        query.artifactDao = Mock(GenericDao, constructorArgs: [recordingClient, Artifact])

        and:
        1 * query.artifactDao.findById(_) >> {
            new Artifact(objectID: 'D1', _type: 'Defect')
        }

        when:
        RallyTask task = query.findTask('1')

        then:
        task.id == 'D1'
        task.type == TaskType.BUG
    }

}
