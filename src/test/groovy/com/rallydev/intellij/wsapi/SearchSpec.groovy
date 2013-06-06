package com.rallydev.intellij.wsapi

import com.rallydev.intellij.SpecUtils
import com.rallydev.intellij.wsapi.domain.Defect
import spock.lang.Shared
import spock.lang.Specification

class SearchSpec extends Specification {

    static String server = 'http://asdf'

    @Shared
    RallyClient recordingClient
    List<String> requests = []

    def setup() {
        recordingClient = Mock(RallyClient)
        recordingClient.makeRequest(_ as GetRequest) >> { GetRequest request ->
            requests << request.getUrl(server.toURL())
            return new ApiResponse(SpecUtils.minimalResponseJson)
        }
    }


    def "search builds query and searches from dao"() {
        given:
        RallyClient rallyClient = recordingClient
        Search search = new Search<>(
                term: 'hello', rallyClient: rallyClient, searchAttributes: ['Name', 'Description'], domainClass: Defect
        )

        when:
        search.doSearch()

        then:
        requests
        requests[0].contains('query=((Name contains "hello") OR (Description contains "hello"))')
    }

}
