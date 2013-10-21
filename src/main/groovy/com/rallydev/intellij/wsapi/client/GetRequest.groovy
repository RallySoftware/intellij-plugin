package com.rallydev.intellij.wsapi.client

import com.rallydev.intellij.wsapi.ApiEndpoint
import groovy.transform.AutoClone
import org.apache.commons.httpclient.util.URIUtil

@AutoClone
class GetRequest implements RallyRequest {
    static int MAX_PAGE_SIZE = 200
    static int MIN_PAGE_SIZE = 1

    ApiEndpoint wsapiObject
    String objectId
    String attribute

    Map<String, String> params = [:]
    Integer startIndex = 1

    GetRequest(ApiEndpoint wsapiObject) {
        this.wsapiObject = wsapiObject
    }

    String getUrl(URL server) {
        "${baseUrl(server)}/${endPoint}${queryString}"
    }

    String getEncodedUrl(URL server) {
        "${baseUrl(server)}/${endPoint}${URIUtil.encodeQuery(queryString)}"
    }

    private String getQueryString() {
        Map<String, String> fullParams = [start: startIndex] + params
        '?' + fullParams.collect { key, value -> "${key}=${value}" }.join('&')
    }

    private String getEndPoint() {
        String endPoint = "${wsapiObject}"
        endPoint += objectId ? "/${objectId}" : ''
        endPoint += ".js"
        endPoint += attribute ? "/${attribute}" : ''

        endPoint
    }

    GetRequest withFetch() {
        params['fetch'] = true
        return this
    }

    GetRequest withPageSize(Integer pageSize) {
        params['pagesize'] = between(pageSize, MIN_PAGE_SIZE, MAX_PAGE_SIZE)
        return this
    }

    GetRequest withStartIndex(Integer startIndex) {
        this.startIndex = startIndex
        return this
    }

    GetRequest withMaxPageSize() {
        return withPageSize(MAX_PAGE_SIZE)
    }

    GetRequest withQuery(String query) {
        params['query'] = query
        return this
    }

    GetRequest withObjectId(String objectId) {
        this.objectId = objectId
        return this
    }

    GetRequest withOrder(String order) {
        params['order'] = order
        return this
    }

    GetRequest withWorkspace(String workspace) {
        params['workspace'] = workspace
        return this
    }

    private baseUrl(URL server) {
        return "${server}/slm/webservice/${RallyClient.WSAPI_VERSION}"
    }

    private int between(int number, int min, int max) {
        Math.min(Math.max(number, min), max)
    }

}
