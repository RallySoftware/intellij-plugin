package com.rallydev.intellij.wsapi.cache

import com.intellij.util.xmlb.XmlSerializer
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.domain.AttributeDefinition
import com.rallydev.intellij.wsapi.domain.TypeDefinition
import org.jdom.Element

class TypeDefinitionCacheSpec extends BaseContainerSpec {

    def "cache is serializable"() {
        given:
        TypeDefinitionCache cache = new TypeDefinitionCache()

        when:
        Element serialized = XmlSerializer.serialize(cache)

        then:
        serialized

        when:
        cache.typeDefinitionsByWorkspace[workspaceRef] =
            new TypeDefinitionCache.TypeDefinitionEntry(typeDefinitions: ['someTypdef': new TypeDefinition()])
        cache.attributeDefinitionsByWorkspace[workspaceRef] =
            new TypeDefinitionCache.AttributeDefinitionEntry(attributeDefinitions: ['someAttribute': [new AttributeDefinition()]])

        and:
        serialized = XmlSerializer.serialize(cache)

        then:
        serialized
    }

}
