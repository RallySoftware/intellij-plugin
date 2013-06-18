package com.rallydev.intellij.wsapi.dao

import com.google.gson.JsonObject
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.GetRequest
import com.rallydev.intellij.wsapi.QueryBuilder
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.ResultList
import com.rallydev.intellij.wsapi.ResultListImpl
import com.rallydev.intellij.wsapi.domain.DomainObject

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

    ResultList<T> find(String order, int pageSize = GetRequest.MAX_PAGE_SIZE) {
        find(null, order, pageSize)
    }

    ResultList<T> find(QueryBuilder query, int pageSize) {
        find(query, null, pageSize)
    }

    ResultList<T> find(QueryBuilder query = null, String order = null, int pageSize = GetRequest.MAX_PAGE_SIZE) {
        GetRequest request = new GetRequest(ApiEndpoint.DOMAIN_CLASS_ENDPOINTS[domainClass])
                .withFetch()
                .withPageSize(pageSize)
        if (order) {
            request.withOrder(order)
        }
        if (query?.hasConditions()) {
            request.withQuery(query.toString())
        }

        ApiResponse response = RallyClient.getInstance().makeRequest(request)
        return new ResultListImpl<T>(domainClass, request, response)
    }

    private T fromSingleResponse(ApiResponse response) {
        T domainObject = domainClass.newInstance()
        JsonObject root = (JsonObject) response.json[domainObject.apiEndpoint.jsonRoot]
        domainObject.assignProperties(root)
        return domainObject
    }

}
