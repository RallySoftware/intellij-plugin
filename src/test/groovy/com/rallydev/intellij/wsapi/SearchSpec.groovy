package com.rallydev.intellij.wsapi

import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.domain.Defect

class SearchSpec extends BaseContainerSpec {

    def "search builds query and searches from dao"() {
        given:
        Search search = new Search<>(
                term: 'hello', searchAttributes: ['Name', 'Description'], domainClass: Defect
        )

        when:
        search.doSearch()

        then:
        recordingClientRequests
        recordingClientRequests[0].contains('query=((Name contains "hello") OR (Description contains "hello"))')
    }

}
