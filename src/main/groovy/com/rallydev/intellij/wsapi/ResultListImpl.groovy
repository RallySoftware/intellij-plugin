package com.rallydev.intellij.wsapi

import com.google.gson.JsonObject
import com.rallydev.intellij.wsapi.client.GetRequest
import com.rallydev.intellij.wsapi.client.RallyClient
import com.rallydev.intellij.wsapi.dao.DaoResponseUnmarshaller
import com.rallydev.intellij.wsapi.domain.DomainObject

class ResultListImpl<T extends DomainObject> extends ArrayList<T> implements ResultList<T> {

    Class<T> domainClass
    Integer totalSize
    Integer nextStartIndex
    GetRequest request

    ResultListImpl(Class<T> domainClass, GetRequest request, ApiResponse response) {
        this.domainClass = domainClass
        this.request = request
        parseResponse(response)
    }

    Boolean getHasMorePages() {
        return nextStartIndex <= totalSize
    }

    void loadAllPages() {
        int loadCount = 0
        while (getHasMorePages() && loadCount++ < MAX_PAGES_TO_LOAD) {
            loadNextPage()
        }
    }

    void loadNextPage() {
        request = request.clone()
        request.startIndex = nextStartIndex

        parseResponse(RallyClient.getInstance().makeRequest(request))
    }

    private void parseResponse(ApiResponse response) {
        response.results.each { JsonObject json ->
            T domainObject = DaoResponseUnmarshaller.instance.buildDomainObject(domainClass, json)
            this << domainObject
        }
        totalSize = response.count
        nextStartIndex = response.pageSize + response.startIndex
    }

}
