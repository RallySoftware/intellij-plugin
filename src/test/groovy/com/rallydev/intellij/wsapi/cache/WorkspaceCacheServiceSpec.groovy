package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.BaseContainerSpec

class WorkspaceCacheServiceSpec extends BaseContainerSpec {

    def setup() {
        registerComponentImplementation(WorkspaceCache)
        registerComponentImplementation(WorkspaceCacheService)
    }

    def 'getInstance returns registered instance'() {
        expect:
        WorkspaceCacheService.getInstance()
        WorkspaceCacheService.getInstance().class == WorkspaceCacheService
    }

    def 'DI is correct for workspace cache setup'() {
        when:
        WorkspaceCacheService cache = ServiceManager.getService(WorkspaceCacheService.class)

        then:
        cache
        cache.rallyClient
    }

    def 'primed should be false when workspaces are not cached'() {
        given:
        WorkspaceCacheService cache = ServiceManager.getService(WorkspaceCacheService.class)

        when: 'only adding loaded on but not workspaces into cache'
        cache.cache.loadedOn = new Date()

        then:
        !cache.getIsPrimed()
    }

    def 'primed true once cache is loaded'() {
        given:
        WorkspaceCacheService cache = ServiceManager.getService(WorkspaceCacheService.class)

        when:
        cache.getCachedWorkspaces()

        then:
        cache.getIsPrimed()
    }

    def 'primed should be false when loaded on is not cached'() {
        given:
        WorkspaceCacheService cache = ServiceManager.getService(WorkspaceCacheService.class)

        when: 'only adding workspace but not loaded on into cache'
        cache.cache.workspaces = []

        then:
        !cache.getIsPrimed()
    }

    def 'clear voids cached workspaces'() {
        given:
        WorkspaceCacheService cache = ServiceManager.getService(WorkspaceCacheService.class)
        cache.getCachedWorkspaces()

        expect:
        cache.getIsPrimed()

        when:
        cache.clear()

        then:
        !cache.getIsPrimed()
    }

}
