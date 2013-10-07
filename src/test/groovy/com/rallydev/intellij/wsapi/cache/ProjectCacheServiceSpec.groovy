package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ResultListMock
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Project

class ProjectCacheServiceSpec extends BaseContainerSpec {

    def setup() {
        registerComponentImplementation(ProjectCache)
        registerComponentImplementation(ProjectCacheService)
    }

    def "getInstance returns registered instance"() {
        expect:
        ProjectCacheService.getInstance()
        ProjectCacheService.getInstance().class == ProjectCacheService
    }

    def "DI is correctly setup"() {
        when:
        ProjectCacheService cache = ServiceManager.getService(ProjectCacheService.class)

        then:
        cache
        cache.rallyClient
    }

    def "projects are loaded from rally"() {
        given:
        ProjectCacheService cache = ServiceManager.getService(ProjectCacheService.class)
        List<Project> wsapiProjects = new ResultListMock([new Project(name: 'Project 1'), new Project(name: 'Project 2')])
        cache.projectDaos[workspaceRef] = Mock(GenericDao, constructorArgs: [Project]) {
            1 * find(_ as String) >> { wsapiProjects }
        }

        when:
        List<Project> projects = cache.getCachedProjects(workspaceRef)

        then:
        projects
        projects.size() == 2
        projects[0].name == wsapiProjects[0].name
        projects[1].name == wsapiProjects[1].name
    }

    def "cached value is used"() {
        given:
        ProjectCacheService cache = ServiceManager.getService(ProjectCacheService.class)
        cache.projectDaos[workspaceRef] = Mock(GenericDao, constructorArgs: [Project])

        when:
        cache.getCachedProjects(workspaceRef)
        cache.getCachedProjects(workspaceRef)
        cache.getCachedProjects(workspaceRef)

        then:
        1 * cache.projectDaos[workspaceRef].find('Name') >> { a ->
            new ResultListMock(["Thing one"])
        }
    }

    def "can clear the cache"() {
        given:
        ProjectCacheService cache = ServiceManager.getService(ProjectCacheService.class)
        cache.projectDaos[workspaceRef] = Mock(GenericDao, constructorArgs: [Project])

        and:
        cache.projectDaos[workspaceRef].find('Name') >> { a ->
            new ResultListMock(["Thing one"])
        }
        cache.getCachedProjects(workspaceRef)

        expect:
        cache.cache.projectsByWorkspace[workspaceRef]

        when:
        cache.clear()

        then:
        !cache.cache.projectsByWorkspace[workspaceRef]
    }

}
