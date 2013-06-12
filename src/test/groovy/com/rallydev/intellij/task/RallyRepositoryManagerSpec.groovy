package com.rallydev.intellij.task

import com.intellij.tasks.youtrack.YouTrackRepository
import com.rallydev.intellij.BaseContainerSpec

class RallyRepositoryManagerSpec extends BaseContainerSpec {

    def "getRepository returns existing RallyRepository"() {
        given:
        def taskManager = GroovySpy(Object) {
            getAllRepositories() >> { [new YouTrackRepository(), new RallyRepository(workspaceId: 7)] }
        }

        and:
        RallyRepositoryManager rallyRepositoryManager = new RallyRepositoryManager(null)
        rallyRepositoryManager.metaClass.getTaskManager = { taskManager }

        when:
        RallyRepository repository = rallyRepositoryManager.getRepository(null)

        then:
        repository
        repository.workspaceId == '7'
    }

    def "getRepository returns new repository if none"() {
        given:
        def taskManager = GroovySpy(Object) {
            getAllRepositories() >> { [] }
        }

        and:
        RallyRepositoryManager rallyRepositoryManager = new RallyRepositoryManager(null)
        rallyRepositoryManager.metaClass.getTaskManager = { taskManager }

        when:
        RallyRepository repository = rallyRepositoryManager.getRepository(null)

        then:
        repository
        repository.workspaceId == '-100'

        and:
        1 * taskManager.setRepositories(_) >> { }
    }

}
