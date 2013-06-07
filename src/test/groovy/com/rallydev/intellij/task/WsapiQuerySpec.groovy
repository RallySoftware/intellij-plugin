package com.rallydev.intellij.task

import com.intellij.tasks.TaskType
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.ResultListMock
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import spock.lang.Specification

class WsapiQuerySpec extends Specification {

    def "findTasks returns list of tasks from requirements and defects"() {
        given:
        WsapiQuery query = new WsapiQuery(Mock(RallyClient))
        query.defectDao = Mock(GenericDao, constructorArgs: [Defect])
        query.requirementDao = Mock(GenericDao, constructorArgs: [Defect])

        and:
        1 * query.defectDao.find(_, _) >> {
            new ResultListMock([new Defect(objectID: 'D1', _type: 'Defect')])
        }
        1 * query.requirementDao.find(_, _) >> {
            new ResultListMock([new Requirement(objectID: 'R1', _type: 'HierarchicalRequirement')])
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
        WsapiQuery query = new WsapiQuery(Mock(RallyClient))
        query.artifactDao = Mock(GenericDao, constructorArgs: [Artifact])

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
