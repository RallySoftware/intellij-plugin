package com.rallydev.intellij.wsapi.cache

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.components.ServiceManager
import com.rallydev.intellij.wsapi.QueryBuilder
import com.rallydev.intellij.wsapi.client.RallyClient
import com.rallydev.intellij.wsapi.ResultList
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.AttributeDefinition
import com.rallydev.intellij.wsapi.domain.TypeDefinition

import static com.rallydev.intellij.wsapi.QueryBuilder.Operator.eq

class TypeDefinitionCacheService {

    TypeDefinitionCache cache

    RallyClient rallyClient
    Map<String, GenericDao<TypeDefinition>> typeDefinitionDaos

    TypeDefinitionCacheService(RallyClient rallyClient, TypeDefinitionCache cache) {
        this.rallyClient = rallyClient
        this.cache = cache

        typeDefinitionDaos = [:].withDefault { String workspaceRef ->
            new GenericDao<TypeDefinition>(TypeDefinition, workspaceRef)
        }
    }

    public static TypeDefinitionCacheService getInstance() {
        return ServiceManager.getService(this)
    }

    TypeDefinition getTypeDefinition(String elementName, String workspaceRef) {
        TypeDefinitionCache.TypeDefinitionEntry typeDefinitionEntry = cache.typeDefinitionsByWorkspace[workspaceRef]
        if (!typeDefinitionEntry) {
            cache.typeDefinitionsByWorkspace[workspaceRef] = typeDefinitionEntry = new TypeDefinitionCache.TypeDefinitionEntry()
        }
        TypeDefinition typedef = typeDefinitionEntry.typeDefinitions[elementName]
        if (!typedef) {
            //todo: Only happy path; add error handling
            ResultList<TypeDefinition> results =
                typeDefinitionDaos[workspaceRef]
                        .find(new QueryBuilder().withConjunction('ElementName', eq, elementName))
            typedef = results[0]
            typedef.attributeDefinitions = loadAttributeDefinitions(typedef, workspaceRef)

            typeDefinitionEntry.typeDefinitions[elementName] = typedef
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

    private Collection<AttributeDefinition> loadAttributeDefinitions(TypeDefinition typedef, String workspaceRef) {
        TypeDefinitionCache.AttributeDefinitionEntry attributeDefinitionEntry = cache.attributeDefinitionsByWorkspace[workspaceRef]
        if (!attributeDefinitionEntry) {
            cache.attributeDefinitionsByWorkspace[workspaceRef] = attributeDefinitionEntry = new TypeDefinitionCache.AttributeDefinitionEntry()
        }

        Collection<AttributeDefinition> attributeDefinitions = attributeDefinitionEntry.attributeDefinitions[typedef.objectID]
        if (!attributeDefinitions) {
            attributeDefinitions = []
            typedef.raw['Attributes'].each { JsonObject raw ->
                raw.add('Workspace', (JsonElement) typedef.raw['Workspace'])
                AttributeDefinition definition = new AttributeDefinition()
                definition.assignProperties(raw)
                attributeDefinitions << definition
            }

            attributeDefinitionEntry.attributeDefinitions[typedef.objectID] = attributeDefinitions
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
