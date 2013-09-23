package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Workspace

class WorkspaceCacheService {

    WorkspaceCache cache
    RallyClient rallyClient
    GenericDao<Workspace> workspaceDao

    WorkspaceCacheService(RallyClient rallyClient, WorkspaceCache cache) {
        this.rallyClient = rallyClient
        this.cache = cache

        workspaceDao = new GenericDao<Workspace>(Workspace, "null")
    }

    public static WorkspaceCacheService getInstance() {
        return ServiceManager.getService(this)
    }

    //todo: Primed still used? Care about date?
    boolean getIsPrimed() {
        cache.workspaces != null &&
                cache.loadedOn != null
    }

    List<Workspace> getCachedWorkspaces() {
        if (!isPrimed) {
            cache.with {
                workspaces = workspaceDao.find('Name')
                workspaces.loadAllPages()
                loadedOn = new Date()
            }
        }
        cache.workspaces
    }

}
