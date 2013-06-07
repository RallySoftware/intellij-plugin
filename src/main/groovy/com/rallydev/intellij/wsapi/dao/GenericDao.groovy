package com.rallydev.intellij.wsapi.dao

import com.google.gson.JsonObject
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.GetRequest
import com.rallydev.intellij.wsapi.QueryBuilder
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.domain.DomainObject

//todo: investigate IntelliJ's provided DI, move RallyClient to injected

class GenericDao<T extends DomainObject> {
    Class<T> domainClass

    GenericDao(Class<T> domainClass) {
        this.domainClass = domainClass
    }

    T findById(String id) {
        GetRequest request = new GetRequest(ApiEndpoint.ARTIFACT)
                .withFetch()
                .withObjectId(id)
        fromSingleResponse(RallyClient.getInstance().makeRequest(request))
    }

    List<T> find(QueryBuilder query = null, int pageSize = GetRequest.MAX_PAGE_SIZE) {
        GetRequest request = new GetRequest(ApiEndpoint.DOMAIN_CLASS_ENDPOINTS[domainClass])
                .withFetch()
                .withPageSize(pageSize)

        if (query?.hasConditions()) {
            request.withQuery(query.toString())
        }

        return fromMultipleResponse(RallyClient.getInstance().makeRequest(request))
    }

    private List<T> fromMultipleResponse(ApiResponse response) {
        List<T> results = []
        response.results.each { JsonObject json ->
            T domainObject = domainClass.newInstance()
            domainObject.assignProperties(json)
            results << domainObject
        }
        return results
    }

    private T fromSingleResponse(ApiResponse response) {
        T domainObject = domainClass.newInstance()
        JsonObject root = (JsonObject) response.json[domainObject.apiEndpoint.jsonRoot]
        domainObject.assignProperties(root)
        return domainObject
    }

}
