package com.rallydev.intellij.wsapi

import com.google.common.util.concurrent.ListenableFuture
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.domain.Requirement

class ResultListSpec extends BaseContainerSpec {

    void setupClient() {
        RallyClient client = Mock(RallyClient) {
            1 * makeRequest(_ as GetRequest) >> {
                Mock(ListenableFuture) {
                    get() >> new ApiResponse(ResultListSpec.classLoader.getResourceAsStream('multiple_requirements2.json').text)
                }
            }
        }
        registerComponentInstance(RallyClient.name, client)
    }

    def "correctly parses api response"() {
        given:
        ApiResponse response = new ApiResponse(ResultListSpec.classLoader.getResourceAsStream('multiple_requirements.json').text)

        when:
        ResultList<Requirement> requirements = new ResultListImpl<Requirement>(Requirement, null, response)

        then:
        requirements.size() == 3

        and:
        requirements[0].objectID == '14345'
        requirements[1].objectID == '14389'
        requirements[2].objectID == '14579'
    }

    def "load data for paging response"() {
        given:
        ApiResponse response = new ApiResponse(ResultListSpec.classLoader.getResourceAsStream('multiple_requirements.json').text)

        when:
        ResultList<Requirement> requirements = new ResultListImpl<Requirement>(Requirement, null, response)

        then:
        requirements.totalSize == 6
        requirements.nextStartIndex == 4
        requirements.hasMorePages
    }

    def "loadNextPage makes and parses correct request and parses response"() {
        given:
        setupClient()

        ApiResponse response = new ApiResponse(ResultListSpec.classLoader.getResourceAsStream('multiple_requirements.json').text)
        GetRequest request = Mock(GetRequest)
        1 * request.clone() >> { request }

        and:
        ResultList<Requirement> requirements = new ResultListImpl<Requirement>(Requirement, request, response)

        when:
        requirements.loadNextPage()

        then:
        1 * request.setStartIndex(4)
        !requirements.hasMorePages
    }

    def "loadAppPages fully loads results"() {
        given:
        setupClient()

        ApiResponse response = new ApiResponse(ResultListSpec.classLoader.getResourceAsStream('multiple_requirements.json').text)
        GetRequest request = Mock(GetRequest)
        request.clone() >> { request }

        and:
        ResultList<Requirement> requirements = new ResultListImpl<Requirement>(Requirement, request, response)

        when:
        requirements.loadAllPages()

        then:
        !requirements.hasMorePages
    }

}
