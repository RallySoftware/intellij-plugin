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
        return ServiceManager.getService(ProjectCacheService.class);
    }

    List<Project> getCachedProjects() {
        if (reload()) {
            projectCache.with {
                projects = projectDao.find('Name')
                projects.loadAllPages()
                loaded = new Date()
                workspace = new Workspace(name: 'hello')
            }
        }
        projectCache.projects
    }

    void clear() {
        projectCache.with {
            projects = null
            loaded = null
            workspace = null
        }
    }

    private boolean reload() {
        projectCache.projects == null ||
                projectCache.loaded == null ||
                (projectCache.loaded + 1) <= new Date()
    }

}
