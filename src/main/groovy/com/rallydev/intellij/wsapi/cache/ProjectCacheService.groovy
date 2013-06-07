package com.rallydev.intellij.wsapi.cache

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

    public List<Project> getCachedProjects() {
        if (reload()) {
            projectCache.projects = projectDao.find('Name')
            projectCache.loaded = new Date()
            projectCache.workspace = new Workspace(name: 'hello')
        }
        projectCache.projects
    }

    private boolean reload() {
        projectCache.projects == null ||
                projectCache.loaded == null ||
                (projectCache.loaded + 1) <= new Date()
    }

}
