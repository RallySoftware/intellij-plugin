package com.rallydev.intellij.config

import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.cache.ProjectCache
import com.rallydev.intellij.wsapi.cache.ProjectCacheService

class InvalidateCachesActionListenerSpec extends BaseContainerSpec {

    def "actionPerformed invalidates caches"() {
        given:
        ProjectCacheService cacheService = Mock(ProjectCacheService, constructorArgs: [recordingClient, Mock(ProjectCache)])
        registerComponentInstance(ProjectCacheService.name, cacheService)

        and:
        InvalidateCachesActionListener listener = new InvalidateCachesActionListener()

        when:
        listener.actionPerformed(null)

        then:
        1 * cacheService.clear() >> { }
    }

}
