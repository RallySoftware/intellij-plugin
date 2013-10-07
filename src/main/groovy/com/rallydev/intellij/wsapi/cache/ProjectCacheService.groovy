package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Project

class ProjectCacheService {
    ProjectCache cache
    RallyClient rallyClient
    Map<String, GenericDao<Project>> projectDaos

    ProjectCacheService(RallyClient rallyClient, ProjectCache cache) {
        this.rallyClient = rallyClient
        this.cache = cache

        projectDaos = [:].withDefault { String workspaceRef ->
            new GenericDao<Project>(Project, workspaceRef)
        }
    }

    public static ProjectCacheService getInstance() {
        return ServiceManager.getService(this)
    }

    List<Project> getCachedProjects(String workspaceRef) {
        List<Project> projects = cache.projectsByWorkspace[workspaceRef]
        if (!projects) {
            projects = cache.projectsByWorkspace[workspaceRef] = projectDaos[workspaceRef].find('Name')
        }
        return projects
    }

    void clear() {
        cache.projectsByWorkspace.clear()
    }

}
