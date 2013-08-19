package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Project
import com.rallydev.intellij.wsapi.domain.Workspace

class ProjectCacheService {

    ProjectCache cache
    RallyClient rallyClient
    GenericDao<Project> projectDao

    ProjectCacheService(RallyClient rallyClient, ProjectCache cache) {
        this.rallyClient = rallyClient
        this.cache = cache

        projectDao = new GenericDao<Project>(Project)
    }

    public static ProjectCacheService getInstance() {
        return ServiceManager.getService(ProjectCacheService)
    }

    boolean getIsPrimed() {
        cache.projects != null &&
                cache.loadedOn != null &&
                (cache.loadedOn + 1) > new Date()
    }

    List<Project> getCachedProjects() {
        if (!isPrimed) {
            cache.with {
                projects = projectDao.find('Name')
                projects.loadAllPages()
                loadedOn = new Date()
                workspace = new Workspace(name: 'hello')
            }
        }
        cache.projects
    }

    void clear() {
        cache.with {
            projects = null
            loadedOn = null
            workspace = null
        }
    }

}
