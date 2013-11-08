package com.rallydev.intellij.wsapi.cache

import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.SpecUtils
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.client.GetRequest
import com.rallydev.intellij.wsapi.client.RallyClient

class WorkspaceCacheServiceSpec extends BaseContainerSpec {

    def setup() {
        recordingClient = Mock(RallyClient)
        recordingClient.makeRequest(_) >> { GetRequest request ->
            recordingClientRequests << request.getUrl(config.url.toURL())
            return new ApiResponse(SpecUtils.getTypedMinimalResponseJson('Workspace'))
        }
        registerComponentInstance(RallyClient.name, recordingClient)

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
