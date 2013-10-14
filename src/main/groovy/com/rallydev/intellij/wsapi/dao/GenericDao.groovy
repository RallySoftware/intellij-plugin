package com.rallydev.intellij.wsapi.dao

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.JsonObject
import com.rallydev.intellij.util.AsyncService
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
    String workspaceRef

    GenericDao(Class<T> domainClass, String workspaceRef = null) {
        this.domainClass = domainClass
        this.workspaceRef = workspaceRef
    }

    T findById(String id) {
        GetRequest request = new GetRequest(ApiEndpoint.ARTIFACT)
                .withFetch()
                .withObjectId(id)
        if(workspaceRef) {
            request.withWorkspace(workspaceRef)
        }
        DaoResponseUnmarshaller.instance.buildDomainObject(domainClass, RallyClient.getInstance().makeRequest(request))
    }

    ResultList<T> find(String order, int pageSize = GetRequest.MAX_PAGE_SIZE) {
        find(null, order, pageSize)
    }

    ResultList<T> find(QueryBuilder query, int pageSize) {
        find(query, null, pageSize)
    }

    ResultList<T> find(QueryBuilder query = null, String order = null, int pageSize = GetRequest.MAX_PAGE_SIZE) {
        GetRequest request = buildRequest(pageSize, order, query)

        ApiResponse response = RallyClient.getInstance().makeRequest(request)
        return new ResultListImpl<T>(domainClass, request, response)
    }

    ListenableFuture<ResultList<T>> findAsync(FutureCallback<ResultListImpl<T>> callback, String order, int pageSize = GetRequest.MAX_PAGE_SIZE) {
        findAsync(callback, null, order, pageSize)
    }

    ListenableFuture<ResultList<T>> findAsync(FutureCallback<ResultListImpl<T>> callback, QueryBuilder query, int pageSize) {
        findAsync(callback, query, null, pageSize)
    }

    ListenableFuture<ResultList<T>> findAsync(FutureCallback<ResultListImpl<T>> callback, QueryBuilder query = null, String order = null, int pageSize = GetRequest.MAX_PAGE_SIZE) {
        GetRequest request = buildRequest(pageSize, order, query)

        Closure<ResultListImpl<T>> apiCall = {
            ApiResponse response = RallyClient.getInstance().makeRequest(request)
            new ResultListImpl<T>(domainClass, request, response)
        }
        ListenableFuture<ResultList<T>> future = AsyncService.instance.schedule(apiCall, callback)
        return future
    }

    private GetRequest buildRequest(int pageSize, String order, QueryBuilder query) {
        GetRequest request = new GetRequest(ApiEndpoint.DOMAIN_CLASS_ENDPOINTS[domainClass])
                .withFetch()
                .withPageSize(pageSize)
        if (order) {
            request.withOrder(order)
        }
        if (query?.hasConditions()) {
            request.withQuery(query.toString())
        }
        if(workspaceRef) {
            request.withWorkspace(workspaceRef)
        }
        request
    }

}
