package com.rallydev.intellij.wsapi.cache

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.QueryBuilder
import com.rallydev.intellij.wsapi.ResultListMock
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.TypeDefinition

import static com.rallydev.intellij.wsapi.ApiEndpoint.DEFECT

class TypeDefinitionCacheServiceSpec extends BaseContainerSpec {

    def setup() {
        registerComponentImplementation(TypeDefinitionCache)
        registerComponentImplementation(TypeDefinitionCacheService)
    }

    def "typeDefinitions are loaded from rally"() {
        given:
        TypeDefinitionCacheService cache = ServiceManager.getService(TypeDefinitionCacheService.class)

        and:
        JsonElement raw = new JsonParser().parse(
                TypeDefinitionCacheServiceSpec.classLoader.getResourceAsStream('defect_typedef.json').text
        )

        and:
        List<TypeDefinition> typeDefinitions = new ResultListMock([
                new TypeDefinition(displayName: 'Bug', raw: raw['QueryResult']['Results']['elements'][0])
        ])
        cache.typeDefinitionDao = Mock(GenericDao, constructorArgs: [TypeDefinition]) {
            1 * find(_ as QueryBuilder) >> { typeDefinitions }
        }

        when:
        TypeDefinition typeDefinition = cache.getTypeDefinition(DEFECT.typeDefinitionElementName)

        then:
        typeDefinition
        typeDefinition.displayName == typeDefinitions[0].displayName

        and: 'attribute definitions are parsed'
        typeDefinition.attributeDefinitions.size() == 120
        typeDefinition.attributeDefinitions.find { it.name == 'Formatted ID' }
        typeDefinition.attributeDefinitions.find { it.name == 'Priority' }
    }

    def "cached value is used"() {
        given:
        TypeDefinitionCacheService cache = ServiceManager.getService(TypeDefinitionCacheService.class)

        and:
        JsonElement raw = new JsonParser().parse(
                TypeDefinitionCacheServiceSpec.classLoader.getResourceAsStream('defect_typedef.json').text
        )

        and:
        List<TypeDefinition> typeDefinitions = new ResultListMock([
                new TypeDefinition(displayName: 'Bug', raw: raw['QueryResult']['Results']['elements'][0])
        ])
        cache.typeDefinitionDao = Mock(GenericDao, constructorArgs: [TypeDefinition])

        when:
        cache.getTypeDefinition(DEFECT.typeDefinitionElementName)
        cache.getTypeDefinition(DEFECT.typeDefinitionElementName)
        cache.getTypeDefinition(DEFECT.typeDefinitionElementName)

        then: 'dao is only called once'
        1 * cache.typeDefinitionDao.find(_ as QueryBuilder) >> { typeDefinitions }
    }

}
