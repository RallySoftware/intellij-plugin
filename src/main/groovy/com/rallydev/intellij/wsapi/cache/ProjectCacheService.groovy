package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Project
import com.rallydev.intellij.wsapi.domain.Workspace

class ProjectCacheService {

    ProjectCache projectCache
    RallyClient rallyClient
    GenericDao<Project> projectDao

    ProjectCacheService(RallyClient rallyClient, ProjectCache projectCache) {
        this.rallyClient = rallyClient
        this.projectCache = projectCache

        projectDao = new GenericDao<Project>(Project.class)
    }

    public static ProjectCacheService getInstance() {
        return ServiceManager.getService(ProjectCacheService.class)
    }

    boolean getIsPrimed() {
        projectCache.projects != null &&
                projectCache.loadedOn != null &&
                (projectCache.loadedOn + 1) > new Date()
    }

    List<Project> getCachedProjects() {
        if (!isPrimed) {
            projectCache.with {
                projects = projectDao.find('Name')
                projects.loadAllPages()
                loadedOn = new Date()
                workspace = new Workspace(name: 'hello')
            }
        }
        projectCache.projects
    }

    void clear() {
        projectCache.with {
            projects = null
            loadedOn = null
            workspace = null
        }
    }

}
