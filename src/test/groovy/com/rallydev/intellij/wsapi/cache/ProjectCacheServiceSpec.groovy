package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ResultListMock
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Project

class ProjectCacheServiceSpec extends BaseContainerSpec {

    def setup() {
        registerComponentImplementation(ProjectCache)
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
        cache.projectDao = Mock(GenericDao, constructorArgs: [Project]) {
            1 * find(_ as String) >> { wsapiProjects }
        }

        when:
        List<Project> projects = cache.cachedProjects

        then:
        projects
        projects.size() == 2
        projects[0].name == wsapiProjects[0].name
        projects[1].name == wsapiProjects[1].name
    }

    def "cached value is used"() {
        given:
        ProjectCacheService cache = ServiceManager.getService(ProjectCacheService.class)
        cache.projectDao = Mock(GenericDao, constructorArgs: [Project])

        when:
        cache.cachedProjects
        cache.cachedProjects
        cache.cachedProjects

        then:
        1 * cache.projectDao.find('Name') >> { new ResultListMock() }
    }

    def "cached value expires after a day"() {
        given:
        ProjectCacheService cache = ServiceManager.getService(ProjectCacheService.class)
        cache.projectDao = Mock(GenericDao, constructorArgs: [Project])

        when:
        cache.cachedProjects
        cache.cachedProjects
        cache.cachedProjects

        then:
        1 * cache.projectDao.find('Name') >> { new ResultListMock() }

        when:
        cache.projectCache.loaded = new Date() - 1

        and:
        cache.cachedProjects

        then:
        1 * cache.projectDao.find('Name') >> { new ResultListMock() }
    }

}
