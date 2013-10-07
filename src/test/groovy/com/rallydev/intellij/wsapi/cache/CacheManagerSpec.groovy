package com.rallydev.intellij.wsapi.cache

import com.rallydev.intellij.BaseContainerSpec

class CacheManagerSpec extends BaseContainerSpec {

    def setup() {
        registerComponentImplementation(CacheManager)
    }

    def "getInstance returns registered instance"() {
        expect:
        CacheManager.instance
        CacheManager.instance.class == CacheManager
    }

    def "clearAllCaches clears project, typedef, and workspace cache"() {
        given: 'Primed caches'
        registerComponentInstance(ProjectCacheService.name, Mock(ProjectCacheService))
        registerComponentInstance(TypeDefinitionCacheService.name, Mock(TypeDefinitionCacheService))
        registerComponentInstance(WorkspaceCacheService.name, Mock(WorkspaceCacheService))

        when:
        CacheManager.instance.clearAllCaches()

        then:
        1 * ProjectCacheService.instance.clear()
        1 * TypeDefinitionCacheService.instance.clear()
        1 * WorkspaceCacheService.instance.clear()
    }

    def "clear sends event to observers"() {
        given:
        Observer observer = Mock(Observer)
        CacheManager.instance.addObserver(observer)

        when:
        CacheManager.instance.clearAllCaches()

        then:
        1 * observer.update(CacheManager.instance, _)
    }

}
