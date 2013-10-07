package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager

class CacheManager extends Observable {

    public static CacheManager getInstance() {
        return ServiceManager.getService(this)
    }

    public clearAllCaches() {
        ProjectCacheService.instance.clear()
        TypeDefinitionCacheService.instance.clear()
        WorkspaceCacheService.instance.clear()

        setChanged()
        notifyObservers()
    }

}
