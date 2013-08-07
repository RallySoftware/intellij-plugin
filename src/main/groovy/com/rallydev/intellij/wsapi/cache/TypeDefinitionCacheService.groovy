package com.rallydev.intellij.wsapi.cache

import com.google.gson.JsonObject
import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.wsapi.QueryBuilder
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.ResultList
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.AttributeDefinition
import com.rallydev.intellij.wsapi.domain.TypeDefinition

import static com.rallydev.intellij.wsapi.QueryBuilder.Operator.eq

class TypeDefinitionCacheService {

    TypeDefinitionCache cache

    RallyClient rallyClient
    GenericDao<TypeDefinition> typeDefinitionDao

    TypeDefinitionCacheService(RallyClient rallyClient, TypeDefinitionCache cache) {
        this.rallyClient = rallyClient
        this.cache = cache

        typeDefinitionDao = new GenericDao<TypeDefinition>(TypeDefinition)
    }

    public static TypeDefinitionCacheService getInstance() {
        return ServiceManager.getService(TypeDefinitionCacheService)
    }

    TypeDefinition getTypeDefinition(String elementName) {
        TypeDefinition typedef = cache.typeDefinitions[elementName]
        if (true /*!typedef*/) {
            //todo: Only happy path; add error handling
            ResultList<TypeDefinition> results =
                typeDefinitionDao.find(new QueryBuilder().withConjunction('ElementName', eq, elementName))
            typedef = results[0]
            typedef.attributeDefinitions = loadAttributeDefinitions(typedef)

            cache.typeDefinitions[elementName] = typedef
        }
        typedef
    }

    //wsapi 2 will require hitting the /Attribute child collection:
    /*
    GetRequest request = new GetRequest(ApiEndpoint.DOMAIN_CLASS_ENDPOINTS[TypeDefinition]).with {
        objectId = typedef.objectID
        attribute = 'Attributes'
        withMaxPageSize()
    }

    ApiResponse response = RallyClient.getInstance().makeRequest(request)
    def results = new ResultListImpl<AttributeDefinition>(AttributeDefinition, request, response)
    results.loadAllPages()
    results
    */
    private Collection<AttributeDefinition> loadAttributeDefinitions(TypeDefinition typedef) {
        Collection<AttributeDefinition> attributeDefinitions = cache.attributeDefinitions[typedef.objectID]
        if (!attributeDefinitions) {
            attributeDefinitions = []
            typedef.raw['Attributes'].each { JsonObject raw ->
                AttributeDefinition definition = new AttributeDefinition()
                definition.assignProperties(raw)
                attributeDefinitions << definition
            }

            cache.attributeDefinitions[typedef.objectID] = attributeDefinitions
        }
        attributeDefinitions
    }

//    void clear() {
//        cache.with {
//            projects = null
//            loadedOn = null
//            workspace = null
//        }
//    }


}
